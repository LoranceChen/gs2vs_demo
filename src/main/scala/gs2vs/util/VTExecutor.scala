package gs2vs.util

import java.util.concurrent.{ExecutorService, Executors}

object VTExecutor {
  val instance: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
}
