package gs2vs.core.diaptcher

import com.lmax.disruptor.EventFactory
import com.lmax.disruptor.dsl.Disruptor
import com.lmax.disruptor.util.DaemonThreadFactory
import org.slf4j.LoggerFactory

/** 使用Disruptor投递消息到actor中，并返回actor消息 保证grpc
  * bi-stream的消息和[[scalasharding.actor.GameActor]]接受及广播消息的顺序性
  */
class Dispatcher(
    inputDisruptor: Disruptor[InputMsg],
    outputDisruptor: Disruptor[OutputMsg]
) {

  /** 发送消息到actor中
    * @return
    */
  def produce(inputMsg: InputMsg) = {
    val ringBuffer = inputDisruptor.getRingBuffer
    ringBuffer.publishEvent(
      (event: InputMsg, sequence: Long, inputMsg: InputMsg) =>
        event.copyFrom(inputMsg),
      inputMsg
    )
  }

  /** 从actor中推送消息到client
    * @param outputMsg
    */
  def pushState(outputMsg: OutputMsg): Unit = {
    val ringBuffer = outputDisruptor.getRingBuffer
    ringBuffer.publishEvent(
      (event: OutputMsg, sequence: Long, outputMsg: OutputMsg) =>
        event.copyFrom(outputMsg),
      outputMsg
    )
  }

  def close(): Unit = {
    Dispatcher.logger.info("Closing dispatcher")
    inputDisruptor.shutdown()
    outputDisruptor.shutdown()
  }

}

object Dispatcher {
  protected val logger =
    LoggerFactory.getLogger(classOf[Dispatcher].getName)

  class InputMsgFactory extends EventFactory[InputMsg] {
    override def newInstance = new InputMsg()
  }
  class OutputMsgFactory extends EventFactory[OutputMsg] {
    override def newInstance = new OutputMsg()
  }

//  case class OutputStateData(msgs: List[String])

  private val BUFFER_SIZE: Int = 1 << 12
  private var dispatcher: Dispatcher = _

  private def create(
  ): Dispatcher = {
    val inputDisruptor = new Disruptor[InputMsg](
      new InputMsgFactory(),
      BUFFER_SIZE,
      DaemonThreadFactory.INSTANCE
    )

    val inputEventConsumerCount =
      Math.min(2, Runtime.getRuntime.availableProcessors)
    val outputEventConsumerCount =
      Math.min(2, Runtime.getRuntime.availableProcessors)

    (0 until inputEventConsumerCount).foreach { index =>
      val consumer =
        new InputEventConsumer(
          index,
          inputEventConsumerCount,
          () => dispatcher
        )
      inputDisruptor.handleEventsWith(consumer)
    }

    val outputDisruptor = new Disruptor[OutputMsg](
      new OutputMsgFactory(),
      BUFFER_SIZE,
      DaemonThreadFactory.INSTANCE
    )
    (0 until outputEventConsumerCount).foreach { index =>
      val consumer =
        new OutputEventConsumer(index, outputEventConsumerCount)
      outputDisruptor.handleEventsWith(consumer)
    }

    inputDisruptor.start()
    outputDisruptor.start()

    this.dispatcher = new Dispatcher(inputDisruptor, outputDisruptor)
    this.dispatcher
  }

  // todo: using DI
  val inst: Dispatcher = this.create()

}
