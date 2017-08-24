package es.jcastro.delfos.scala

import java.io.File
import java.lang.management.ManagementFactory
import java.util.Random

import es.jcastro.delfos.scala.common.Chronometer
import es.jcastro.delfos.scala.evaluation._
import es.jcastro.delfos.scala.evaluation.intest.{NDCG_inTest, Precision_inTest}
import es.jcastro.delfos.scala.evaluation.overall.{NDCG_overall, Precision_overall}
import org.apache.commons.cli.{CommandLine, Options}
import org.apache.commons.io.FileUtils
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by jcastro on 10/08/2017.
  */
object Main extends App {




  override def main(args: Array[String]) {

    var cmd  = consoleParser(args)

    println("Executing spark-single-user-rs")

    val sparkConfiguration = new SparkConf()
      .setAppName("spark-single-user-rs")

    // Let's create the Spark Context using the configuration we just created
    val sc = new SparkContext(sparkConfiguration)


    val localCheckpointDir = "./checkpoint/"
    var checkpointDir = if (isMachine("corbeta-jcastro-debian"))
      localCheckpointDir
      else cmd.getOptionValue("checkpointDir",localCheckpointDir)

    sc.setCheckpointDir(checkpointDir)

    val ratings = getRatings(sc, cmd)

    val kRange = getKRange(cmd)
    val minK:Int = kRange._1
    val maxK:Int = kRange._2

    println("Dataset has '"+ratings.count()+"' ratings")

    // Build the recommendation model using ALS
    val rank = 20
    val numIterations = 100

    val seed:Long = 0l

    val ratingsWithRandom:RDD[(Rating,Double)] = ratings
      .map(rating => {
        val thisRatingSeed = rating.hashCode()+seed
        val random = new Random(thisRatingSeed)
        val randomValue:Double =random.nextDouble()
        (rating, randomValue)
      })

    val trainingRatio :Double= 0.8

    val ratingsTraining:RDD[Rating] = ratingsWithRandom.filter(entry => {
      val training:Boolean = entry._2 <= trainingRatio
      training
    }).map(_._1).cache()
    println("\ttrain: "+ratingsTraining.count())

    val ratingsTest:RDD[Rating] = ratingsWithRandom.filter(entry => {
      val training:Boolean = entry._2 > trainingRatio
      training
    }).map(_._1).cache()
    println("\ttest: "+ratingsTest.count())

    println("Building ALS model ")
    val chronometer = new Chronometer

    val model:MatrixFactorizationModel = if(cmd.hasOption("isImplicit") || cmd.hasOption("makeImplicit") )
      ALS.trainImplicit(ratingsTraining,rank, numIterations,0.01,1)
    else
      ALS.train(ratingsTraining, rank, numIterations, 0.01)

    println("\tdone in "+chronometer.printTotalElapsed)

    val users:RDD[Int] = ratings.map(_.user).distinct()
    val products:RDD[Int] = ratings.map(_.product).distinct()

    println("#users:    "+users.count())
    println("#products: "+products.count())

    val ratingsTraining_inDriver:Set[(Int,Int)] = ratingsTraining.map(rating => (rating.user,rating.product)).collect().toSet
    println("#ratings:  "+ratingsTraining_inDriver.size)

    val usersProductsReduced:RDD[(Int,Int)] = ratingsTest.map(r=> (r.user,r.product)).cache()
    val predictionsReduced:RDD[((Int,Int),Double)] =
      model.predict(usersProductsReduced).map { case Rating(user, product, rate) =>
        ((user, product), rate)
      }.cache()

    val ratingsTestTuples: RDD[((Int,Int),Double)] = ratingsTest
      .map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }

    val ratesAndPreds:RDD[((Int,Int),(Double,Double))] = ratingsTestTuples.join(predictionsReduced).cache()

    val mse:Double = MSE.getMeasure(ratesAndPreds)
    println("Mean Squared Error = " + mse)
    val mae:Double = MAE.getMeasure(ratesAndPreds)
    println("Mean Absolute Error = " + mae)

    println("Computing NDCG with separate user computation")
    val model_my:MatrixFactorizationModel_my = new MatrixFactorizationModel_my(
      model.userFeatures.collect().toMap,
      model.productFeatures.collect().toMap
    )

    
    val str:StringBuilder = new StringBuilder()

    if(!cmd.hasOption("makeImplicit") && !cmd.hasOption("isImplicit"))
      computeMeasuresOnTestProducts(ratesAndPreds, minK, maxK,str)

    val ratingsTrainTestByUser:RDD[(Int,(Iterable[Rating],Iterable[Rating]))] =
      ratingsTraining.groupBy(_.user).join(ratingsTest.groupBy(_.user)).cache()

    computeMeasuresOnAllProducts(ratingsTrainTestByUser, model_my, minK, maxK,str)

    println(str.toString())
  }

  private def computeMeasuresOnTestProducts(ratesAndPreds: RDD[((Int, Int), (Double, Double))], minK: Int, maxK: Int, str:StringBuilder) = {
    (minK to maxK).foreach(k => {
      val value = NDCG_inTest.getMeasure(ratesAndPreds, k)

      val msg: String = "NDCG_inTest at " + k + " = " + value
      println(msg)
      str.append(msg + "\n")
    })

    (minK to maxK).foreach(k => {
      val value = Precision_inTest.getMeasure(ratesAndPreds, k)

      val msg: String = "Precision_inTest at " + k + " = " + value
      println(msg)
      str.append(msg + "\n")
    })
  }

  private def computeMeasuresOnAllProducts(ratingsTrainTestByUser:RDD[(Int,(Iterable[Rating],Iterable[Rating]))], model_my: MatrixFactorizationModel_my, minK: Int, maxK: Int, str:StringBuilder) = {
    (minK to maxK).foreach(k => {
      val value = NDCG_overall.getMeasure(ratingsTrainTestByUser, model_my, k)
      val msg: String = "NDCG_overall at " + k + " = " + value
      println(msg)
      str.append(msg + "\n")
    })

    (minK to maxK).foreach(k => {
      val value = Precision_overall.getMeasure(ratingsTrainTestByUser, model_my, k)

      val msg: String = "Precision_overall at " + k + " = " + value
      println(msg)
      str.append(msg + "\n")
    })
  }

  def saveModel(filePath:String, sc:SparkContext, model:MatrixFactorizationModel) ={
    val modelName:String = filePath.replaceAll("/","-")

    val modelPath: String = "als-models/" ++ modelName

    if(new File(modelPath).exists() && new File(modelPath).isDirectory)
      FileUtils.deleteDirectory(new File(modelPath))

    model.save(sc,modelPath)
    val sameModel = MatrixFactorizationModel.load(sc, modelPath)
  }

  def isMachine(str: String):Boolean     ={
    val names = ManagementFactory.getRuntimeMXBean().getName().split('@')

    val process = names(0)
    val machine = names(1)

    val isMachine = machine.equals(str)

    isMachine
  }
  def consoleParser(args:Array[String])={

    val options = new Options()

    val input =  new org.apache.commons.cli.Option("i","input",true,"input file path" )
    input.setRequired(true)
    options.addOption(input)

    val makeImplicit = new org.apache.commons.cli.Option("mim","makeImplicit",false,"convert input as implicit" )
    makeImplicit.setRequired(false)
    options.addOption(makeImplicit)

    val isImplicit = new org.apache.commons.cli.Option("iim","isImplicit",false,"treat input as implicit --> expect only 2tuples in the input file" )
    isImplicit.setRequired(false)
    options.addOption(isImplicit)

    val checkpointDir = new org.apache.commons.cli.Option("c","checkpointDir",true,"specify the checkpoint dir for spark" )
    checkpointDir.setRequired(false)
    options.addOption(checkpointDir)

    val numPartitions = new org.apache.commons.cli.Option("p","numPartitions", true,"specify the number of partitions for the ratings dataset")
    numPartitions.setRequired(false)
    numPartitions.setArgs(1)
    options.addOption(numPartitions)

    val k = new org.apache.commons.cli.Option("k","kRange", true, "range of top-k recommendations to be evaluated")
    k.setRequired(false)
    k.setArgs(2)
    options.addOption(k)

    import org.apache.commons.cli.HelpFormatter
    val parser = new org.apache.commons.cli.BasicParser()
    val formatter = new HelpFormatter

    var cmd : CommandLine = null
    try
      cmd = parser.parse(options, args)
    catch {
      case e: Exception =>
        System.out.println(e.getMessage)
        formatter.printHelp("utility-name", options)
        System.exit(1)
    }
    cmd
  }

  def getNumPartitions(cmd: CommandLine):Int = {
    val numPartitions = cmd.getOptionValue("numPartitions","32")
    numPartitions.toInt
  }

  def getRatings(sc: SparkContext, cmd: CommandLine) :RDD[Rating] = {
    val filePath : String = cmd.getOptionValue("input")
    sc.addFile(filePath)

    val makeImplicit: Boolean = cmd.hasOption("makeImplicit")
    val isImplicit: Boolean = cmd.hasOption("isImplicit")

    // Load and parse the data
    val data = sc.textFile(filePath,getNumPartitions(cmd))

    println("Default parallelism = "+sc.defaultParallelism)
    println("data num partitions = "+ data.getNumPartitions)

    if(isImplicit ||makeImplicit){
      val ratings:RDD[Rating] = data.map(_.split('\t'))
        .map(record => {
          val user    = record(0).toInt
          val product = record(1).toInt
          val rating  = 5.0

          new Rating(user,product,rating)
        })
      ratings
    }else {
      val ratings: RDD[Rating] = data.map(_.split('\t'))
        .map(record => {
          val user = record(0).toInt
          val product = record(1).toInt
          val rating = record(2).toDouble

          new Rating(user, product, rating)
        })
      ratings
    }
  }

  def getKRange(cmd: CommandLine): (Int,Int) = {
    val minK = if(cmd.hasOption("kRange")) cmd.getOptionValues("kRange")(0).toInt else 1
    val maxK = if(cmd.hasOption("kRange")) cmd.getOptionValues("kRange")(1).toInt else 100

    if(minK <= 0 || maxK <=0 )
      throw new IllegalArgumentException("k range cannot contain negative numbers or zero")

    if(minK < maxK)
      (minK,maxK)
    else
      (maxK,minK)
  }
}
