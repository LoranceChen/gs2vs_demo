package gs2vs.wsservice

import gs2vs.core.Dependencies
import gs2vs.core.wsservice.AService
import gs2vs.core.wsservice.AService.ServiceIndex
import gs2vs.dependency.ServiceDeps
import gs2vs.protoindex.ServiceProtoConst

object WsServices {
  lazy val allService = Map[ServiceIndex, AService](
    ServiceProtoConst.USER_SERVICE -> Dependencies.getGsObject(
      classOf[ServiceDeps],
      classOf[UserService]
    ), // ServiceDeps.userServiceLive,
    ServiceProtoConst.PGSQL_SERVICE -> Dependencies.getGsObject(
      classOf[ServiceDeps],
      classOf[PgSqlService]
    )
  )
}
