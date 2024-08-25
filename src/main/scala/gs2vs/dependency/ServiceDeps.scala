package gs2vs.dependency
import gs2vs.DepsModule
import gs2vs.core.Dependencies
import gs2vs.core.db.DBDriver
import gs2vs.core.redis.RedisStringCommand
import gs2vs.wsservice.{PgSqlService, UserService}

class ServiceDeps extends DepsModule {
  private lazy val userServiceLive: UserService = new UserService(
    Dependencies
      .getGsObject(
        classOf[DBDeps],
        classOf[DBDriver]
      ),
    null
//    Dependencies
//      .getGsObject(
//        classOf[RedisDeps],
//        classOf[RedisStringCommand]
//      )
  )

  private lazy val pgSqlServiceLive: PgSqlService = new PgSqlService(
    Dependencies
      .getGsObject(
        classOf[DBDeps],
        classOf[DBDriver]
      )
  )

  override lazy val defineObjects: List[(Class[_], Any)] =
    List(
      classOf[UserService] -> userServiceLive,
      classOf[PgSqlService] -> pgSqlServiceLive
    )
}
