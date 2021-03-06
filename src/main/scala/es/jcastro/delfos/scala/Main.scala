package es.jcastro.delfos.scala

import java.io.File
import java.lang.management.ManagementFactory
import java.util

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

    val ratings = getRatings(sc, cmd).cache()

    val kRange = getKRange(cmd)
    val minK:Int = kRange._1
    val maxK:Int = kRange._2

    val users:RDD[Int] = ratings.map(_.user).distinct()
    val products:RDD[Int] = ratings.map(_.product).distinct()

    println("#users:    "+users.count())
    println("#products: "+products.count())
    println("#ratings:  "+ratings.count())

    // Build the recommendation model using ALS
    val rank = cmd.getOptionValue("rank","20").toInt
    if(rank <= 0 )
      throw new IllegalArgumentException("rank must be strictly positive")

    val numIterations = cmd.getOptionValue("numIterations","100").toInt
    if(numIterations <= 0 )
      throw new IllegalArgumentException("numIterations must be strictly positive")

    val seed:Long = cmd.getOptionValue("seed","0").toLong
    val trainingRatio :Double= cmd.getOptionValue("trainingRatio","0.8").toDouble
    if(trainingRatio<0 || trainingRatio>1.0){
      throw new IllegalArgumentException("trainingRatio must be in [0,1]")
    }

    val trainTestPartitions = ratings.randomSplit(Array(trainingRatio, 1- trainingRatio), seed = seed)

    val ratingsTraining:RDD[Rating] = trainTestPartitions(0).cache()
    val ratingsTest:RDD[Rating] = trainTestPartitions(1).cache()

    val chronometer = new Chronometer

    val model:MatrixFactorizationModel = if(cmd.hasOption("implicitFeedback") ) {
      println("Building implicit ALS model ")
      ALS.trainImplicit(
        ratings = ratingsTraining,
        rank = rank,
        iterations = numIterations,
        lambda = 0.01,
        blocks = -1,
        alpha = 0.01,
        seed = seed)
    }else {
      println("Building ALS model ")
      ALS.train(
        ratings = ratingsTraining,
        rank = rank,
        iterations = numIterations,
        lambda = 0.01,
        blocks = -1,
        seed = seed)
    }

    println("\tdone in "+chronometer.printTotalElapsed)

    val usersProductsReduced:RDD[(Int,Int)] = ratingsTest.map(r=> (r.user,r.product)).cache()
    val predictionsReduced:RDD[((Int,Int),Double)] =
      model.predict(usersProductsReduced).map { case Rating(user, product, rate) =>
        ((user, product), rate)
      }.cache()

    val ratingsTestTuples: RDD[((Int,Int),Double)] = ratingsTest
      .map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }

    val ratesAndPreds: RDD[((Int, Int), (Double, Double))] = ratingsTestTuples.join(predictionsReduced).cache()
    if(!cmd.hasOption("implicitFeedback")) {
      val mse: Double = MSE.getMeasure(ratesAndPreds)
      println("Mean Squared Error = " + mse)
      val mae: Double = MAE.getMeasure(ratesAndPreds)
      println("Mean Absolute Error = " + mae)
    }

    println("Computing NDCG with separate user computation")
    val model_my:MatrixFactorizationModel_my = new MatrixFactorizationModel_my(
      model.userFeatures.collect().toMap,
      model.productFeatures.collect().toMap
    )

    if(!cmd.hasOption("implicitFeedback")) {
      computeMeasuresOnTestProducts(ratesAndPreds, minK, maxK)
    }

    val ratingsTrainTestByUser:RDD[(Int,(Iterable[Rating],Iterable[Rating]))] =
      ratingsTraining.groupBy(_.user).join(ratingsTest.groupBy(_.user)).cache()

    computeMeasuresOnAllProducts(ratingsTrainTestByUser, model_my, minK, maxK)
  }

  private def computeMeasuresOnTestProducts(ratesAndPreds: RDD[((Int, Int), (Double, Double))], minK: Int, maxK: Int) = {
    (minK to maxK).foreach(k => {
      val value = NDCG_inTest.getMeasure(ratesAndPreds, k)

      val msg: String = "NDCG_inTest at " + k + " = " + value
      println(msg)
    })

    (minK to maxK).foreach(k => {
      val value = Precision_inTest.getMeasure(ratesAndPreds, k)

      val msg: String = "Precision_inTest at " + k + " = " + value
      println(msg)
    })
  }

  private def computeMeasuresOnAllProducts(ratingsTrainTestByUser:RDD[(Int,(Iterable[Rating],Iterable[Rating]))], model_my: MatrixFactorizationModel_my, minK: Int, maxK: Int) = {
    (minK to maxK).foreach(k => {
      val value = NDCG_overall.getMeasure(ratingsTrainTestByUser, model_my, k)
      val msg: String = "NDCG_overall at " + k + " = " + value
      println(msg)
    })

    (minK to maxK).foreach(k => {
      val value = Precision_overall.getMeasure(ratingsTrainTestByUser, model_my, k)

      val msg: String = "Precision_overall at " + k + " = " + value
      println(msg)
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

    val input =  new org.apache.commons.cli.Option("i","input",true,"location of the input file that contains the ratings dataset" )
    input.setRequired(true)
    options.addOption(input)

    val checkpointDir = new org.apache.commons.cli.Option("c","checkpointDir",true,"specify the checkpoint directory for spark" )
    checkpointDir.setRequired(false)
    options.addOption(checkpointDir)

    val numPartitions = new org.apache.commons.cli.Option("p","numPartitions", true,"specify the number of partitions that spark makes of the RDD for the ratings dataset")
    numPartitions.setRequired(false)
    numPartitions.setArgs(1)
    options.addOption(numPartitions)

    val debug = new org.apache.commons.cli.Option("d","debug", false,"print debug messages")
    debug.setRequired(false)
    options.addOption(debug)

    val k = new org.apache.commons.cli.Option("k","kRange", true, "range of top-k recommendations to be evaluated (two positive integer values)")
    k.setRequired(false)
    k.setArgs(2)
    options.addOption(k)

    val trainingRatio = new org.apache.commons.cli.Option("trainingRatio", true, "ratio of ratings included in the training dataset")
    trainingRatio.setRequired(false)
    trainingRatio.setArgs(1)
    options.addOption(trainingRatio)

    val lambda = new org.apache.commons.cli.Option("lambda", true, "ALS regularization factor (recommended: 0.01)")
    lambda.setRequired(false)
    lambda.setArgs(1)
    options.addOption(lambda)

    val rank = new org.apache.commons.cli.Option("r","rank", true, "number of features to use in the ALS model")
    rank.setRequired(false)
    rank.setArgs(1)
    options.addOption(rank)

    val numIterations = new org.apache.commons.cli.Option("n","numIterations", true, "number of iterations of ALS (recommended: 10-20)")
    numIterations.setRequired(false)
    numIterations.setArgs(1)
    options.addOption(numIterations)

    val seed = new org.apache.commons.cli.Option("s","seed", true, "seed used in the ALS model generation")
    seed.setRequired(false)
    seed.setArgs(1)
    options.addOption(seed)

    val implicitFeedback = new org.apache.commons.cli.Option("if","implicitFeedback",false,"generate ALS model taking input as implicit (ignore rating value)" )
    implicitFeedback.setRequired(false)
    options.addOption(implicitFeedback)

    val alpha = new org.apache.commons.cli.Option("alpha", true, "ALS confidence parameter (only applies for implicitFeedback=true)")
    alpha.setRequired(false)
    alpha.setArgs(1)
    options.addOption(alpha)

    val help = new org.apache.commons.cli.Option("h","help", true, "print the help page")
    help.setRequired(false)
    help.setArgs(1)
    options.addOption(help)

    val list = options.getOptions
      .asInstanceOf[util.Collection[org.apache.commons.cli.Option]]

    val newList = new util.ArrayList[org.apache.commons.cli.Option](list)

    val parser = new org.apache.commons.cli.BasicParser()
    val formatter = new org.apache.commons.cli.HelpFormatter

    var cmd : CommandLine = null
    try
      cmd = parser.parse(options, args)
    catch {
      case e: Exception =>
        System.out.println(e.getMessage)
        formatter.printHelp("spark-single-user-rs", options)
        System.exit(1)
    }

    if(cmd.hasOption("help")){
      formatter.printHelp("spark-single-user-rs", options)
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

    val implicitFeedback: Boolean = cmd.hasOption("implicitFeedback")

    // Load and parse the data
    val data = if(cmd.hasOption("numPartitions"))
      sc.textFile(filePath,getNumPartitions(cmd))
    else
      sc.textFile(filePath)

    if(cmd.hasOption("debug")) {
      println("Default parallelism = " + sc.defaultParallelism)
      println("data num partitions = " + data.getNumPartitions)
    }

    if(implicitFeedback){
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
    val maxK = if(cmd.hasOption("kRange")) cmd.getOptionValues("kRange")(1).toInt else 20

    if(minK <= 0 || maxK <=0 )
      throw new IllegalArgumentException("k range cannot contain negative numbers or zero")

    if(minK < maxK)
      (minK,maxK)
    else
      (maxK,minK)
  }
}
