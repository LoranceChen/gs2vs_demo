package gs2vs.core.wsserver

case class WsContext(wsId: String, sequence: Int, requestTime: Long) {
  val getMsgId = s"${wsId}_${sequence}"
}

object WsContext {
  private[core] val USER_CONTEXT = ScopedValue.newInstance[WsContext]()
  private val USER_CONTEXT_THREAD_LOCAL = new ThreadLocal[WsContext]()

  def getUser() = {
    if (Thread.currentThread().isVirtual) {
      USER_CONTEXT.get()
    } else {
      USER_CONTEXT_THREAD_LOCAL.get()
    }
  }

  def setUser(wsContext: WsContext): Unit = {
    if (Thread.currentThread().isVirtual) {
      throw new UnsupportedOperationException(
        "virtual thread cannot set thread local"
      )
    } else {
      USER_CONTEXT_THREAD_LOCAL.set(wsContext)
    }
  }

  def removeUser(): Unit = {
    if (Thread.currentThread().isVirtual) {
      throw new UnsupportedOperationException(
        "virtual thread cannot set thread local"
      )
    } else {
      USER_CONTEXT_THREAD_LOCAL.remove()
    }
  }
}
