package es.jcastro.delfos.scala.evaluation.overall

import es.jcastro.delfos.scala.MatrixFactorizationModel_my
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD

object Precision_overall {

  def getMeasure(ratingsTrain:RDD[Rating],ratingsTest:RDD[Rating],model:MatrixFactorizationModel_my, k:Int) :Double={

    val products:Set[Int] = model.productsFeatures.keySet

    val ratingsTrainByUser:Map[Int, Iterable[Rating]] = ratingsTrain.groupBy(_.user).collect().toMap

    val precision_byUser:Map[Int,Double] = ratingsTest.groupBy(_.user).map(entry=>{
      val user:Int = entry._1
      val userTestRatings:Map[Int,Rating] = entry._2
        .map(rating => (rating.product,rating))
        .toMap

      val userTrainRatings:Map[Int,Rating] = ratingsTrainByUser
        .getOrElse(user,Iterable.empty[Rating])
        .map(rating => (rating.product,rating))
        .toMap

      val itemsRated:Set[Int] = userTrainRatings.keySet
      val notRated: Set[Int] = products.filter(!itemsRated.contains(_))

      if(userTestRatings.isEmpty)
        (user,Double.NaN)

      val userPredictionsWithRating: Seq[(Int, Double, Double)] =notRated.toSeq
        .map(product => {

          val prediction:Double  = model.predict(user,product).getOrElse(0.0)
          val rating: Double = userTestRatings.get(product).map(_.rating).getOrElse(0.0)

          (product,prediction,rating)
        })

      val byPrediction: Ordering[(Int, Double, Double)] = Ordering.by(_._2)

      val bestByPrediction: Seq[(Int, Double, Double)] = userPredictionsWithRating
        .sorted(byPrediction).reverse
        .slice(0, k)

      val userPredictions_actual: Seq[Double] = bestByPrediction.map(_._3)

      val precision = precisionAtK(userPredictions_actual, k)

      (user, precision)
    })
      .filter(entry => {!Double.NaN.equals(entry._2)})
      .collect().toMap

    val size:Double = precision_byUser.size

    val precision:Double = precision_byUser.map(_._2).map(_/size).sum

    precision
  }

  def getMeasure( predictions : RDD[((Int, Int), (Double))], ratings : RDD[Rating], k:Int):Double = {
    val ratingsByUser:Map[Int,Iterable[Rating]] = ratings.groupBy(_.user).collect().toMap
    val predictionsByUser: RDD[(Int, Iterable[((Int,Int),Double)])] = predictions.groupBy(_._1._1)

    getMeasure(predictionsByUser,ratingsByUser,k)
  }

  def getMeasure( predictionsByUser : RDD[(Int, Iterable[((Int,Int),Double)])], ratingsByUser:Map[Int,Iterable[Rating]], k:Int):Double ={

    val precision_byUser:Seq[(Int, Double)] = predictionsByUser.map(userPredictions => {

      val user = userPredictions._1

      val userRatings:Map[Int,Double] = ratingsByUser.get(user)
        .getOrElse(Seq.empty[Rating])
        .map(rating => (rating.product,rating.rating))
        .toMap

      if(userRatings.isEmpty)
        (user,Double.NaN)
      else {
        val userPredictionsWithRating: Seq[(Int, Double, Double)] = userPredictions._2
          .filter(_._1._1 == user)
          .map(entry => {

            val product: Int = entry._1._2
            val prediction: Double = entry._2
            val rating: Double = userRatings.get(product).getOrElse(0.0)

            (product, prediction, rating)
          }).toSeq

        val byPrediction: Ordering[(Int, Double, Double)] = Ordering.by(_._2)

        val bestByPrediction: Seq[(Int, Double, Double)] = userPredictionsWithRating
          .sorted(byPrediction).reverse
          .slice(0, k)

        val userPredictions_actual: Seq[Double] = bestByPrediction.map(_._3)

        val precision = precisionAtK(userPredictions_actual, k)

        (user, precision)
      }
    }).collect().toSeq

    val precision_byUser_noNaNs:Seq[Double] = precision_byUser.map(_._2)
      .filter(!Double.NaN.equals(_))

    val precision_sum:Double = precision_byUser_noNaNs.sum

    val precision:Double = precision_sum/precision_byUser.length

    precision
  }

  def precisionAtK(list:Seq[Double], k:Integer):Double = {
    val listOfK = list.slice(0,k)

    var length:Int = Math.min(k,listOfK.length);

    val itemCounts:Seq[Double] = ( 0 to length-1).map(i => {
        val relevant = if(listOfK(i) >= 4.0) 1.0 else 0.0
        relevant
      }).toSeq

    val precision:Double = itemCounts.sum / itemCounts.size

    precision
  }

  def log2(value:Double): Double ={
    val log2OfValue = Math.log(value)/Math.log(2)

    log2OfValue
  }

}
