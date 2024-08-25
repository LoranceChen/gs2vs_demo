package gs2vs
import gs2vs.core.Dependencies

object GS2Main {
//  val tracing = new TracingUtils()
//  val otel = tracing.openTelemetry

  def main(args: Array[String]): Unit = {
    // set up dependency
    Dependencies.init
    Dependencies.start

    println("hello! virtual thread")
    new WsServer().start()
    Thread.currentThread().join()
  }

}
