package gs2vs

trait DepsModule {
  protected val gsObjects: collection.mutable.HashMap[Class[_], Any] =
    new collection.mutable.HashMap[Class[_], Any]()

  // should be defined as lazy in implement
  protected val defineObjects: List[(Class[_], Any)]

  lazy val init: DepsModule = {
    defineObjects.foreach { case (clazz, item) =>
      if (!gsObjects.contains(clazz))
        gsObjects.put(clazz, item)
    }
    this
  }

  def getObject[T](clazz: Class[T]): T = {
    gsObjects.get(clazz) match {
      case None =>
        throw new RuntimeException(s"get objects fail. ${clazz.getName}")
      case Some(value) => value.asInstanceOf[T]
    }
  }

  // test unit only
  def injectObject[T](clazz: Class[_], value: T): Unit = {
    this.gsObjects.update(clazz, value)
  }

}
