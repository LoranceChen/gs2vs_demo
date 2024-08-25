package gs2vs.wsservice
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import org.slf4j.LoggerFactory

trait ProtoJsonUtils {
  val serviceName: String
  protected val logger =
    LoggerFactory.getLogger(this.getClass.getName)

  private val jsonFormat: JsonFormat.Printer =
    JsonFormat.printer().omittingInsignificantWhitespace()

  def handleClientLog[Req <: MessageOrBuilder, Rsp <: MessageOrBuilder](
      actionName: String,
      request: => Req,
      ops: Req => Rsp
  ): Rsp = {
    val req = request
    val rsp = ops(req)
    logger.info(
      s"handle request. serviceName: ${serviceName}, actionName: ${actionName}, request: ${jsonFormat
          .print(req)}, reponse: ${jsonFormat.print(rsp)}"
    )
    rsp
  }
}
