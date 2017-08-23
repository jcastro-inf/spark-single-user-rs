package es.jcastro.delfos.scala

import java.io.File
import java.lang.management.ManagementFactory
import java.util.Random

import es.jcastro.delfos.scala.common.Chronometer
import es.jcastro.delfos.scala.evaluation._
import es.jcastro.delfos.scala.evaluation.intest.{NDCG_inTest, Precision_inTest}
import es.jcastro.delfos.scala.evaluation.overall.{NDCG_overall, Precision_overall}
import org.apache.commons.io.FileUtils
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by jcastro on 10/08/2017.
  */
object Main extends App {

  override def main(args: Array[String]) {
    println("Executing spark-single-user-rs")

    val sparkConfiguration = new SparkConf()
      .setAppName("spark-single-user-rs")

    // Let's create the Spark Context using the configuration we just created
    val sc = new SparkContext(sparkConfiguration)


    val hdfsCheckpointDir = "hdfs://192.168.10.27:8020/spark-single-user-grs/checkpoints"
    val localCheckpointDir = "./checkpoint/"

    var checkpointDir = if (isMachine("corbeta-jcastro-debian"))
      localCheckpointDir
      else hdfsCheckpointDir

    sc.setCheckpointDir(checkpointDir)

    val filePath : String =args(0)

    sc.addFile(filePath)

    // Load and parse the data
    val data = sc.textFile(filePath)

    val ratings:RDD[Rating] = data.map(_.split('\t') match { case Array(user, product, rate,timestamp) =>
      Rating(user.toInt, product.toInt, rate.toDouble)
    })

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
    val model:MatrixFactorizationModel = ALS.train(ratingsTraining, rank, numIterations, 0.01)
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

    val minK:Int = 1
    val maxK:Int = 100

    val str:StringBuilder = new StringBuilder()

    //computeMeasuresOnTestProducts(ratesAndPreds, minK, maxK,str)

    computeMeasuresOnAllProducts(ratingsTraining, ratingsTest, model_my, minK, maxK,str)

    println(str.toString())

    saveModel(filePath,sc,model)
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

  private def computeMeasuresOnAllProducts(ratingsTraining: RDD[Rating], ratingsTest: RDD[Rating], model_my: MatrixFactorizationModel_my, minK: Int, maxK: Int, str:StringBuilder) = {
    (minK to maxK).foreach(k => {
      val value = NDCG_overall.getMeasure(ratingsTraining, ratingsTest, model_my, k)
      val msg: String = "NDCG_overall at " + k + " = " + value
      println(msg)
      str.append(msg + "\n")
    })

    (minK to maxK).foreach(k => {
      val value = Precision_overall.getMeasure(ratingsTraining, ratingsTest, model_my, k)

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
}
