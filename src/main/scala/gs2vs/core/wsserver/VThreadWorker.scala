package gs2vs.core.wsserver

import gs2vs.services.game.protobuf.CommonData
import gs2vs.core.diaptcher.InputEventConsumer.Msg
import gs2vs.core.diaptcher.{Dispatcher, InputMsg, OutputMsg}
import gs2vs.protoindex.ServiceProtoConst
import gs2vs.wsservice.WsServices
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.context.Scope
import org.slf4j.LoggerFactory

import java.util.concurrent.{ArrayBlockingQueue, TimeUnit}

class VThreadWorker(dispatcher: Dispatcher) {
  protected val logger = LoggerFactory.getLogger(classOf[VThreadWorker].getName)

  private val timeout = 100

  def routing(queue: ArrayBlockingQueue[InputMsg]): Unit = {
    var inputMsg: InputMsg = null
    while (true) {
      while(inputMsg == null) {
        inputMsg = queue.poll(timeout, TimeUnit.MILLISECONDS)
//        println(s"try get some thing. ${inputMsg}")
      }

      try {
        val tracer =
          GlobalOpenTelemetry.getTracer("application") // the agent way
//        val tracer = GS2Main.otel.getTracer(classOf[VThreadWorker].getName, "0.1.0")
        val span = tracer
          .spanBuilder(
            ServiceProtoConst.namedService
              .getOrElse(
                inputMsg.serviceIndex,
                inputMsg.serviceIndex.toString
              ) + "." + inputMsg.actionIndex
          )
          .startSpan
        var scope: Scope = null

        val userContextScopeValue = ScopedValue.where(
          WsContext.USER_CONTEXT,
          WsContext(
            wsId = inputMsg.wsId,
            sequence = inputMsg.sequence,
            requestTime = inputMsg.timeNano
          )
        )

        userContextScopeValue.run(() => {
          try {
            scope = span.makeCurrent()
            val allService = WsServices.allService
            val service = allService(inputMsg.serviceIndex)
            val action = service.actions(inputMsg.actionIndex)
            val response = action(inputMsg.msgPB)

            val output = new OutputMsg()
            output.wsId = inputMsg.wsId
            output.entityHashId = inputMsg.entityHashId
            output.head = 0
            output.serviceIndex = inputMsg.serviceIndex
            output.actionIndex = inputMsg.actionIndex
            output.sequence = inputMsg.sequence
            output.msgPb = response.toByteArray
            output.beginTimeNano = Some(inputMsg.timeNano)
            dispatcher.pushState(output)
          } catch {
            case e: Throwable =>
              span.recordException(e)
              logger.error(s"process ws request fail.", e)
              val output = new OutputMsg()
              output.wsId = inputMsg.wsId
              output.entityHashId = inputMsg.entityHashId
              output.head = 1
              output.serviceIndex = inputMsg.serviceIndex
              output.actionIndex = inputMsg.actionIndex
              output.sequence = inputMsg.sequence
              output.failPb = CommonData.FailMessage
                .newBuilder()
                .setErrorCode(1001)
                .setDesc(e.getMessage)
                .build()
                .toByteArray
              output.beginTimeNano = Some(inputMsg.timeNano)
              dispatcher.pushState(output)
          } finally {
            if (scope != null) {
              scope.close()
              span.end()
            }
          }
        })

      } catch {
        case e: Throwable =>
          logger.error(s"process ws request fail.", e)
          val output = new OutputMsg()
          output.wsId = inputMsg.wsId
          output.entityHashId = inputMsg.entityHashId
          dispatcher.pushState(output)
      } finally {
        inputMsg = null
      }
    }

  }

  private def parseMsg(msg: String): Msg = {
    val items = msg.split(";").toList
    Msg(serviceName = items(0), actionName = items(1), param = items(2))
  }

}
