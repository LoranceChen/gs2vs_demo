//package gs2vs.dependency
//import scala.collection.mutable
//
//trait THas {}
//
///** define a calculate to get module of M and value object of V.
//  * [[Dependencies.start]] will recursively inject data after init
//  * @tparam T
//  */
//case class Has[M, V](private var v: V) extends THas {
//  lazy val getValue: V = {}
//}
//
//object Has {
//  val undefined: THas = UnDefined
//
//  val cache = new mutable.HashMap[(Class[_], Class[_]), Any]()
//  def build() = {}
//}
//
//case class FinalHas[M <: DepsModule, V](m: M, v: V) extends THas {
//  lazy val getValue: Unit = {
//    v = Dependencies.getGsObject(classOf[M], classOf[V])
//  }
//}
//
//case object UnDefined extends THas
