package gs2vs.dependency

import gs2vs.DepsModule
import gs2vs.core.grpcsvr.GrpcServer
import gs2vs.grpcsvc.GrpcService

class GrpcServiceDeps extends DepsModule {
  private lazy val grpcService: GrpcService = new GrpcService()

  private lazy val grpcServer: GrpcServer = {
    val service = new GrpcServer()
    service.newServer(grpcService)
    service
  }

  override lazy val defineObjects: List[(Class[_], Any)] =
    List(
      classOf[GrpcService] -> grpcService,
      classOf[GrpcServer] -> grpcServer
    )
}
