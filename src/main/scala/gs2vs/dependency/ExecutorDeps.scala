package gs2vs.dependency
import gs2vs.DepsModule
import gs2vs.core.utils.ExecutorUtils.DbExecutor
import gs2vs.core.{Config, Dependencies}

import java.util.concurrent.Executors

class ExecutorDeps extends DepsModule {
  private lazy val dbExecutor: DbExecutor = DbExecutor(
    Executors.newFixedThreadPool(
      Dependencies
        .getGsObject(classOf[ConfDeps], classOf[Config])
        .jdbcConfig
        .threadPoolSize
    )
  )

  override protected lazy val defineObjects: List[(Class[_], Any)] = List(
    classOf[DbExecutor] -> dbExecutor
  )
}
