package es.jcastro.delfos.scala.evaluation

import org.apache.spark.rdd.RDD

object MSE {

  def getMeasure( ratesAndPreds : RDD[((Int, Int), (Double,Double))]):Double ={

    val count:Double = ratesAndPreds.count();

    val measureValue = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
      val err = (r1 - r2)
      err*err
    }.map(err => err/count).sum()

    measureValue
  }

}
