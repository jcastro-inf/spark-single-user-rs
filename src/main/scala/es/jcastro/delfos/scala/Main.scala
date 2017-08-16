package es.jcastro.delfos.scala

import java.io.File

import es.jcastro.delfos.scala.evaluation._
import org.apache.commons.io.FileUtils
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random

/**
  * Created by jcastro on 10/08/2017.
  */
object Main {

  def main(args: Array[String]) {

    val sparkConfiguration = new SparkConf().
      setAppName("spark-single-user-rs")

    // Let's create the Spark Context using the configuration we just created
    val sc = new SparkContext(sparkConfiguration)
    sc.setCheckpointDir("checkpoint/")

    val filePath : String = "/home/jcastro/Dropbox/Datasets-new/ml-100k/u.data"

    sc.addFile(filePath)

    // Load and parse the data
    val data = sc.textFile(filePath)

    val ratings = data.map(_.split('\t') match { case Array(user, product, rate,timestamp) =>
      Rating(user.toInt, product.toInt, rate.toDouble)
    })

    // Build the recommendation model using ALS
    val rank = 20
    val numIterations = 100

    val random = new Random(0)

    val ratingsWithRandom:Array[(Rating,Double)] = ratings.collect()
      .map(rating => (rating, random.nextDouble()))

    val ratingsTraining_array = ratingsWithRandom.filter(entry => {
      val training:Boolean = entry._2 <= 0.8
      training
    }).map(_._1).toSeq
    val ratingsTest_array = ratingsWithRandom.filter(entry => {
      val training:Boolean = entry._2 > 0.8
      training
    }).map(_._1).toSeq

    val ratingsTraining:RDD[Rating] = sc.parallelize(ratingsTraining_array)
    val ratingsTest:RDD[Rating] = sc.parallelize(ratingsTest_array)

    val model = ALS.train(ratingsTraining, rank, numIterations, 0.01)

    val users = ratings.map(_.user).distinct()
    val products = ratings.map(_.product).distinct()

    val ratingsTraining_inDriver:Set[(Int,Int)] = ratingsTraining.map(rating => (rating.user,rating.product)).collect().toSet
    val usersProducts = users.cartesian(products)
      .filter(rating => !ratingsTraining_inDriver.contains((rating._1,rating._2)))

    val predictions =
      model.predict(usersProducts).map { case Rating(user, product, rate) =>
        ((user, product), rate)
      }

    val ratingsTestTuples: RDD[((Int,Int),Double)] = ratingsTest.map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }

    val ratesAndPreds = ratingsTestTuples.join(predictions)


    val mse:Double = MSE.getMeasure(ratesAndPreds)
    println("Mean Squared Error = " + mse)
    val mae:Double = MAE.getMeasure(ratesAndPreds)
    println("Mean Absolute Error = " + mae)


    val predictionsByUser: RDD[(Int, Iterable[((Int,Int),Double)])] = predictions.groupBy(_._1._1)
    val ratingsByUser:Map[Int,Iterable[Rating]] = ratings.groupBy(_.user).collect().toMap

    val maxK:Int = 100

    val str:StringBuilder = new StringBuilder()

    val ndcg_byK = (1 to maxK).foreach(k => {
      val value = NDCG.getMeasure(predictionsByUser,ratingsByUser,k)
      val msg:String = "NDCG at "+k+" = "+value;
      println(msg)
      str.append(msg+"\n")
    })

    val ndcg_overall_byK = (1 to maxK).foreach(k => {
      val value = NDCG_overall.getMeasure(predictionsByUser,ratingsByUser,k)

      val msg:String = "NDCG_overall at "+k+" = "+value;
      println(msg)
      str.append(msg+"\n")
    })

    val precision_byK = (1 to maxK).foreach(k => {
      val value = Precision.getMeasure(predictionsByUser,ratingsByUser,k)

      val msg:String = "Precision at "+k+" = "+value;
      println(msg)
      str.append(msg+"\n")
    })

    val precision_overall_byK = (1 to maxK).foreach(k => {
      val value = Precision_overall.getMeasure(predictionsByUser,ratingsByUser,k)

      val msg:String = "Precision_overall at "+k+" = "+value;
      println(msg)
      str.append(msg+"\n")
    })


    println(str.toString())

    val modelName:String = filePath.replaceAll("/","-")

    val modelPath: String = "als-models/" ++ modelName

    if(new File(modelPath).exists() && new File(modelPath).isDirectory)
      FileUtils.deleteDirectory(new File(modelPath))

    model.save(sc,modelPath)
    val sameModel = MatrixFactorizationModel.load(sc, modelPath)
  }
}
