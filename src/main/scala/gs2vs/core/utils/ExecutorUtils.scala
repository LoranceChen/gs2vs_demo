package gs2vs.core.utils
import gs2vs.core.wsserver.WsContext

import java.util.concurrent.{CompletableFuture, ExecutorService}

object ExecutorUtils {
  sealed trait GS2Executor {
    protected val instance: ExecutorService

    def runAndWait[T](task: => T): T = {
      if (!Thread.currentThread().isVirtual)
        throw new RuntimeException(
          "[ERROR] Submit task fail. Only support virtual thread wait task"
        )

      val context = WsContext.getUser()
      val future = new CompletableFuture[T]()
      instance.execute(() => {
        WsContext.setUser(context)
        try{ future.complete(task) } finally {
          WsContext.removeUser()
        }
      })

      future.get()
    }

    def execute(task: => Unit): Unit = {
      instance.execute(() => task)
    }
  }

  case class OsExecutor(instance: ExecutorService) extends GS2Executor
  case class VtExecutor(instance: ExecutorService) extends GS2Executor
  case class DbExecutor(instance: ExecutorService) extends GS2Executor
}
