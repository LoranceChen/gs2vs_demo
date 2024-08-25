package gs2vs
import gs2vs.dependency._

object GsDepModules {

  // developer put new module here! Initialize by sequence
  val modules = List(
    new ConfDeps,
//    new ProfilerDeps,
    new ExecutorDeps,
    new DBDeps,
    new RedisDeps,
    new ServiceDeps,
    new GrpcServiceDeps
  )

}
