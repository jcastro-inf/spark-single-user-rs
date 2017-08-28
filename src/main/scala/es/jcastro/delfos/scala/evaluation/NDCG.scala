package es.jcastro.delfos.scala.evaluation

object NDCG {



  def dcgAtK(list:Seq[Double], k:Integer):Double = {
    val listOfK = list.slice(0,k)

    var length:Int = Math.min(k,listOfK.length);

    val itemCounts = ( 1 to length).map(rank => {
      val discount = (1/log2(rank+1))
      val thisItemCount = listOfK(rank-1) * discount
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
