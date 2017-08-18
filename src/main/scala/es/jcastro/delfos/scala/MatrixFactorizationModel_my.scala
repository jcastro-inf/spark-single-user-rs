package es.jcastro.delfos.scala

class MatrixFactorizationModel_my(
                                   _usersFeatures: Map[Int,Array[Double]],
                                  _productsFeatures : Map[Int,Array[Double]]) extends Serializable {

  val productsFeatures: Map[Int,Array[Double]] = _productsFeatures
  val usersFeatures: Map[Int,Array[Double]] = _usersFeatures

  def predict(user:Int,product:Int):Option[Double] = {

    val userFeatures = usersFeatures.getOrElse(user,Array.emptyDoubleArray)
    val productFeatures = productsFeatures.getOrElse(product,Array.emptyDoubleArray)

    if(userFeatures.isEmpty ||productFeatures.isEmpty) {
      None
    }else if(userFeatures.size != productFeatures.size) {
      throw new IllegalStateException("Feature vectors have different sizes")
    }else {
      val dotProduct=(0 to userFeatures.size - 1).map(index => userFeatures(index) * productFeatures(index))
      val prediciton = dotProduct.sum

      Option(prediciton)
    }
  }

}
