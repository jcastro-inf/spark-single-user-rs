package es.jcastro.delfos.scala

import java.io.File

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

//    // Evaluate the model on rating data
//    val usersProducts = ratingsTest.map { case Rating(user, product, rate) =>
//      (user, product)
//    }

    val predictions =
      model.predict(usersProducts).map { case Rating(user, product, rate) =>
        ((user, product), rate)
      }


    val ratingsTestTuplesAll : RDD[((Int,Int),Double)] = usersProducts.map(entry => {

      val rating:Option[Rating]= ratingsTest_array
        .find(rating=> rating.user==entry._1 && rating.product == entry._2)
      val ratingValue = rating.map(rating=> rating.rating).getOrElse(Double.NaN)

      (entry,ratingValue)
    })
    val ratingsTestTuples: RDD[((Int,Int),Double)] = ratingsTest.map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }

    val ratesAndPreds = ratingsTestTuples.join(predictions)
    val predsAndRates = predictions.join(ratingsTestTuples)

    val predictions_inHead:Array[((Int,Int),Double)] = predictions.collect()
    val ratesAndPreds_inHead:Array[((Int,Int),(Double,Double))] = ratesAndPreds.collect()

    val mse:Double = es.jcastro.delfos.scala.evaluation.MSE.getMeasure(ratesAndPreds)
    val mae:Double = es.jcastro.delfos.scala.evaluation.MSE.getMeasure(ratesAndPreds)

    println("Mean Squared Error = " + mse)
    println("Mean Absolute Error = " + mae)

    val modelName:String = filePath.replaceAll("/","-")

    val modelPath: String = "als-models/" ++ modelName

    if(new File(modelPath).exists() && new File(modelPath).isDirectory)
      FileUtils.deleteDirectory(new File(modelPath))

    model.save(sc,modelPath)
    val sameModel = MatrixFactorizationModel.load(sc, modelPath)
  }
}
