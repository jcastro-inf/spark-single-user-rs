package es.jcastro.delfos.scala.evaluation.intest

import org.apache.spark.rdd.RDD

object NDCG_inTest {

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

      val byRating: Ordering[(Int, Double, Double)] = Ordering.by(_._3)
      val byPrediction: Ordering[(Int, Double, Double)] = Ordering.by(_._2)

      val bestByPrediction: Seq[(Int, Double, Double)] = userPredictionsWithRating
        .sorted(byPrediction).reverse
        .slice(0, k)

      val bestByRating: Seq[(Int, Double, Double)] = userPredictionsWithRating
        .sorted(byRating).reverse
        .slice(0, k)

      val userPredictions_actual: Seq[Double] = bestByPrediction.map(_._3)
      val userPredictions_perfect: Seq[Double] = bestByRating.map(_._3)

      val dcg_actual = dcgAtK(userPredictions_actual, k)
      val dcg_perfect = dcgAtK(userPredictions_perfect, k)

      val ndcg = dcg_actual / dcg_perfect

      (user, ndcg)

    })

    val ndcg_byUser_noNaNs:RDD[Double] = ndcg_byUser.map(_._2)
      .filter(!Double.NaN.equals(_))

    val ndcg_sum:Double = ndcg_byUser_noNaNs.sum

    val ndcg:Double = ndcg_sum/ndcg_byUser.count()

    ndcg
  }

  def dcgAtK(list:Seq[Double], k:Integer):Double = {
    val listOfK = list.slice(0,k)

    var length:Int = Math.min(k,listOfK.length);

    val itemCounts = ( 0 to length-1).map(i => {
        val discount = if (i>= 2) (1/log2(i+1)) else 1.0
        val thisItemCount = listOfK(i) * discount
        thisItemCount
      })

    val dcg = itemCounts.sum

    dcg
  }

  def log2(value:Double): Double ={
    val log2OfValue = Math.log(value)/Math.log(2)

    log2OfValue
  }

}
