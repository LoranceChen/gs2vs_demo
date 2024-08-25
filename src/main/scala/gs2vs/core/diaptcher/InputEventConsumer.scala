package gs2vs.core.diaptcher

import com.lmax.disruptor.EventHandler
import gs2vs.core.diaptcher.InputEventConsumer.WsSession
import gs2vs.core.wsserver.VThreadWorker
import org.slf4j.LoggerFactory

import java.util.concurrent.{
  ArrayBlockingQueue,
  BlockingQueue,
  ConcurrentHashMap
}

class InputEventConsumer(
    index: Int,
    inputEventConsumerCount: Int,
    dispatcher: () => Dispatcher
) extends EventHandler[InputMsg] {

  protected val logger =
    LoggerFactory.getLogger(classOf[InputEventConsumer].getName)

  val manager = new ConcurrentHashMap[String, WsSession]()

  val vthreadFactory = Thread.ofVirtual().name("gs2worker-", 1).factory()

  override def onEvent(
      event: InputMsg,
      sequence: Long,
      endOfBatch: Boolean
  ): Unit = {
    if (Math.abs(event.entityHashId) % inputEventConsumerCount != index) return
    val queueSize = 1 << 12

    val wsSession = manager.computeIfAbsent(
      event.wsId,
      _entitId => {
        val queue = new ArrayBlockingQueue[InputMsg](queueSize)
        // a websocket instance
        val vThread: Thread = vthreadFactory.newThread(new Runnable {
          override def run(): Unit = {
            // loop logic
            new VThreadWorker(dispatcher()).routing(queue)
          }
        })
        vThread.start()
        val wsId = event.wsId
        WsSession(wsId, queue, vThread)
      }
    )
    if (wsSession.queue.size() < queueSize) {
      wsSession.queue.put(event)
    } else {
      logger.warn("drop event because of queue is full")
    }
  }

}

object InputEventConsumer {
  case class WsSession(
      wsId: String,
      queue: BlockingQueue[InputMsg],
      vThread: Thread
  )

  case class Msg(serviceName: String, actionName: String, param: String)

}
