package gs2vs.core.diaptcher

import com.lmax.disruptor.EventHandler
import gs2vs.core.wsserver.WSocketManager
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import org.slf4j.LoggerFactory

class OutputEventConsumer(index: Int, outputEventConsumerCount: Int)
    extends EventHandler[OutputMsg] {
  protected val logger = LoggerFactory.getLogger(classOf[OutputEventConsumer])

  override def onEvent(
      event: OutputMsg,
      sequence: Long,
      endOfBatch: Boolean
  ): Unit = {
    if (Math.abs(event.entityHashId) % outputEventConsumerCount != index) return

    val channel = WSocketManager.concurrentHashMap.get(event.wsId)
    if (channel == null) {
      println(s"OutputEventConsumer.onEvent channel is null. ${event.wsId}")
    } else {
      var buf: ByteBuf = null
      val isSuccess = (event.head & 1) == 0
      if (!isSuccess) { // fail
        buf = Unpooled.buffer(1 + 2 + 1 + 4 + event.failPb.length)
        buf.writeByte(event.head)
      } else { // success
        buf = Unpooled.buffer(1 + 2 + 1 + 4 + event.msgPb.length)
        buf.writeByte(event.head)
      }
      buf.writeShortLE(event.serviceIndex)
      buf.writeByte(event.actionIndex)
      buf.writeIntLE(event.sequence)
      if (isSuccess) {
        buf.writeBytes(event.msgPb)
      } else {
        buf.writeBytes(event.failPb)
      }

      // endTime
      event.beginTimeNano.foreach(timeNano => {
        val endTime = System.nanoTime()
        val timeCost = endTime - timeNano
//        logger.info(s"end time: $endTime, begin time: $timeNano")

        logger.debug(
          s"[TIME_COST_ANALYSIS] PROTO[${event.serviceIndex}.${event.actionIndex}:${event.sequence}] wsId: ${event.wsId}, beginTime: ${timeNano}, endTime: ${endTime} server total time cost: $timeCost"
        )
      })
      channel.writeAndFlush(
        new BinaryWebSocketFrame(buf)
      )
    }
  }

}
