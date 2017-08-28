package es.jcastro.delfos.scala.evaluation.intest

import es.jcastro.delfos.scala.evaluation.NDCG
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

      val dcg_actual = NDCG.dcgAtK(userPredictions_actual, k)
      val dcg_perfect = NDCG.dcgAtK(userPredictions_perfect, k)

      val ndcg = dcg_actual / dcg_perfect
      (user, ndcg)
    })

    val ndcg_byUser_noNaNs:RDD[Double] = ndcg_byUser.map(_._2)
      .filter(!Double.NaN.equals(_))

    val ndcg_sum:Double = ndcg_byUser_noNaNs.sum

    val ndcg:Double = ndcg_sum/ndcg_byUser.count()

    ndcg
  }
}
