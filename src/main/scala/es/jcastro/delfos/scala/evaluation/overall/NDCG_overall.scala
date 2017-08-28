package es.jcastro.delfos.scala.evaluation.overall

import es.jcastro.delfos.scala.MatrixFactorizationModel_my
import es.jcastro.delfos.scala.evaluation.NDCG
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD

import collection.{Map, Set}

object NDCG_overall {

  def getMeasure(ratingsTrainTestByUser:RDD[(Int,(Iterable[Rating],Iterable[Rating]))],model:MatrixFactorizationModel_my, k:Int) :Double={

    val products:Set[Int] = model.productsFeatures.keySet

    val ndcg_byUser:Array[(Int,Double)] = ratingsTrainTestByUser
      .map(userData => {
        val user = userData._1
        val userTrainingData = userData._2._1
        val userTestsData = userData._2._2
        getMeasureThisUser(k, products, model,user, userTrainingData,userTestsData)
      })
      .filter(entry => {!Double.NaN.equals(entry._2)})
      .collect()

    val size:Double = ndcg_byUser.size

    val ndcg:Double = ndcg_byUser.map(_._2).map(_/size).sum

    ndcg
  }

  def getMeasureThisUser(
                          k: Int,
                          products: Set[Int],
                          model: MatrixFactorizationModel_my,
                          user: Int,
                          ratingsTrain:Iterable[Rating],
                          ratingsTest:Iterable[Rating]
                        ): (Int, Double) = {

    val userTestRatings: Map[Int, Rating] = ratingsTest
      .map(rating => (rating.product, rating))
      .toMap

    val userTrainRatings: Map[Int, Rating] = ratingsTrain
      .map(rating => (rating.product, rating))
      .toMap

    val itemsRated: Set[Int] = userTrainRatings.keySet
    val notRated: Set[Int] = products.filter(!itemsRated.contains(_))

    if (userTestRatings.isEmpty)
      (user, Double.NaN)

    val userPredictionsWithRating: Seq[(Int, Double, Double)] = notRated.toSeq
      .map(product => {

        val prediction: Double = model.predict(user, product).getOrElse(0.0)
        val rating: Double = userTestRatings.get(product).map(_.rating).getOrElse(0.0)

        (product, prediction, rating)
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

    val dcg_actual = NDCG.dcgAtK(userPredictions_actual, k)
    val dcg_perfect = NDCG.dcgAtK(userPredictions_perfect, k)

    val ndcg = dcg_actual / dcg_perfect

    (user, ndcg)
  }

  def getMeasure(predictions : RDD[((Int, Int), (Double))], ratings : RDD[Rating], k:Int):Double = {
    val ratingsByUser:Map[Int,Iterable[Rating]] = ratings.groupBy(_.user).collect().toMap
    val predictionsByUser: RDD[(Int, Iterable[((Int,Int),Double)])] = predictions.groupBy(_._1._1)

    getMeasure(predictionsByUser,ratingsByUser,k)
  }

  def getMeasure( predictionsByUser : RDD[(Int, Iterable[((Int,Int),Double)])], ratingsByUser:Map[Int,Iterable[Rating]], k:Int):Double ={

    val ndcg_byUser:RDD[(Int, Double)] = predictionsByUser.map(userPredictions => {

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
      }
    })

    val ndcg_byUser_noNaNs:RDD[Double] = ndcg_byUser.map(_._2)
      .filter(!Double.NaN.equals(_))

    val ndcg_sum:Double = ndcg_byUser_noNaNs.sum

    val ndcg:Double = ndcg_sum/ndcg_byUser.count()

    ndcg
  }

}
