package gs2vs.core

import gs2vs.{DepsModule, GsDepModules}

object Dependencies {
  private val staticRegisteredDepsModule =
    new collection.mutable.HashMap[Class[_], DepsModule]()

  lazy val init: Unit = {
    GsDepModules.modules.foreach(item => {
      staticRegisteredDepsModule.put(item.getClass, item)
    })
  }

  // used on test
  def mockPrepare[T <: DepsModule, V](
      moduleClazz: Class[T],
      gsObject: V
  ): Unit = {
    this.injectObject(moduleClazz, gsObject)
  }

  // not support cycle deps
  lazy val start = {
    GsDepModules.modules.foreach(item => item.init)
  }

  def getDepsModuleOpt[T <: DepsModule](`class`: Class[T]): Option[T] = {
    // should not be non
    staticRegisteredDepsModule.get(`class`).map(_.asInstanceOf[T])
  }

  def getDepsModule[T <: DepsModule](`class`: Class[T]): T = {
    getDepsModuleOpt(`class`) match {
      case None =>
        throw new RuntimeException(
          s"get module fail. module: ${`class`.getName}"
        )
      case Some(value) => value
    }
  }

  def getGsObject[T <: DepsModule, V](
      clazz: Class[T],
      clazzV: Class[V]
  ): V = {
    val module: T = getDepsModule(clazz)
    module.getObject[V](clazzV)
  }

  def injectObject[T <: DepsModule, V](
      clazz: Class[T],
      value: V
  ): Unit = {
    val module: T = getDepsModule(clazz)
    module.injectObject(value.getClass, value)
  }

  trait Env
  object Env {
    case object Prod extends Env
    case object Stg extends Env
    case object Dev extends Env
    case object Test extends Env
  }

}
