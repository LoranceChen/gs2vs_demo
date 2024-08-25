package gs2vs.grpcsvc
import gs2vs.services.game.protobuf.GrpcServiceOuterClass.{HelloStreamRequest, HelloStreamResponse}
import gs2vs.services.game.protobuf.{GrpcServiceGrpc, GrpcServiceOuterClass}
import io.grpc.stub.{ServerCallStreamObserver, StreamObserver}
import org.slf4j.LoggerFactory

class GrpcService extends GrpcServiceGrpc.GrpcServiceImplBase {
  val logger = LoggerFactory.getLogger(classOf[GrpcService])

  override def helloStream(
      responseObserver: StreamObserver[
        GrpcServiceOuterClass.HelloStreamResponse
      ]): StreamObserver[GrpcServiceOuterClass.HelloStreamRequest] = {
    logger.info("connected")
    new StreamObserver[HelloStreamRequest] {
      override def onNext(
          value: HelloStreamRequest
      ): Unit = {
        try {
//          logger.info("onNext: " + value.getMsg + ", seq: " + value.getSequence)
          responseObserver.synchronized(
            responseObserver.onNext(
              HelloStreamResponse.newBuilder().setEcho(value.getMsg).setSequence(value.getSequence).build()
            )
          )
        } catch {
          case t: Throwable =>
            logger.error("fail some where. ", t)
        }
      }

      override def onError(t: Throwable): Unit = {
        try{
          logger.info("onerror: " + t.getMessage)
          responseObserver.synchronized(responseObserver.onError(t))
        } catch {
          case t: Throwable =>
            logger.error("fail some where. ", t)
        }
      }


      override def onCompleted(): Unit = {
        try{
          logger.info("completed")
          responseObserver.synchronized(responseObserver.onCompleted())
        } catch {
          case t: Throwable =>
            logger.error("fail some where. ", t)
        }
      }
     }
  }
}
