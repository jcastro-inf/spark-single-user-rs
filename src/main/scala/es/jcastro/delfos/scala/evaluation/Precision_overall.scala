package es.jcastro.delfos.scala.evaluation

import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD

object Precision_overall {


  def getMeasure( ratesAndPreds:RDD[((Int,Int),(Double,Double))], k:Int):Double ={

    val ndcg_byUser:RDD[(Int, Double)] = ratesAndPreds.groupBy(_._1._1).map(userPredictions => {

      val user = userPredictions._1

      val userPredictionsWithRating: Seq[(Int, Double, Double)] = userPredictions._2
        .filter(_._1._1 == user)
        .map(entry => {

          val product: Int = entry._1._2
          val rating: Double = entry._2._1
          val prediction: Double = entry._2._2

          (product, prediction, rating)
        }).toSeq

      val byPrediction: Ordering[(Int, Double, Double)] = Ordering.by(_._2)

      val bestByPrediction: Seq[(Int, Double, Double)] = userPredictionsWithRating
        .sorted(byPrediction).reverse
        .slice(0, k)

      val userPredictions_actual: Seq[Double] = bestByPrediction.map(_._3)

      val precision = precisionAtK(userPredictions_actual, k)

      (user, precision)

    })

    val ndcg_byUser_noNaNs:RDD[Double] = ndcg_byUser.map(_._2)
      .filter(!Double.NaN.equals(_))

    val ndcg_sum:Double = ndcg_byUser_noNaNs.sum

    val ndcg:Double = ndcg_sum/ndcg_byUser.count()

    ndcg
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
