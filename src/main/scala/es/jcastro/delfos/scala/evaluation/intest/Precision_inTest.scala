package es.jcastro.delfos.scala.evaluation.intest

import org.apache.spark.rdd.RDD

object Precision_inTest {

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
}
