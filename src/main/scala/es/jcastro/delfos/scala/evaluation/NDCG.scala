package es.jcastro.delfos.scala.evaluation

import es.jcastro.delfos.scala.MatrixFactorizationModel_my
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD

object NDCG {

  def getMeasure(ratingsTrain:RDD[Rating],ratingsTest:RDD[Rating],model:MatrixFactorizationModel_my, k:Int) :Double={

    val products:Set[Int] = model.productsFeatures.keySet

    val ratingsTrainByUser:Map[Int, Iterable[Rating]] = ratingsTrain.groupBy(_.user).collect().toMap
    val ratingsTestByUser:Map[Int, Iterable[Rating]] = ratingsTest.groupBy(_.user).collect().toMap

    val ndcg_byUser:Map[Int,Double] = ratingsTest.groupBy(_.user).map(entry=>{
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
      .filter(entry => {!Double.NaN.equals(entry._2)})
      .collect().toMap

    val size:Double = ndcg_byUser.size

    val ndcg:Double = ndcg_byUser.map(_._2).map(_/size).sum

    ndcg
  }

  def getMeasure( predictionsByUser : RDD[(Int, Iterable[((Int,Int),Double)])], ratingsByUser:Map[Int,Iterable[Rating]], k:Int):Double ={

    val ndcg_byUser:Seq[(Int, Double)] = predictionsByUser.map(userPredictions => {

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
          .filter(entry => userRatings.contains(entry._1._2))
          .map(entry => {

            val product: Int = entry._1._2
            val prediction: Double = entry._2
            val rating: Double = userRatings.get(product).getOrElse(0.0)

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
      }
    }).collect().toSeq

    val ndcg_byUser_noNaNs:Seq[Double] = ndcg_byUser.map(_._2)
      .filter(!Double.NaN.equals(_))

    val ndcg_sum:Double = ndcg_byUser_noNaNs.sum

    val ndcg:Double = ndcg_sum/ndcg_byUser.length

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
