package gs2vs.dependency
import gs2vs.DepsModule
import gs2vs.core.redis.RedisStringCommand
import gs2vs.core.{Config, Dependencies}
import io.lettuce.core.{RedisClient, RedisURI}

class RedisDeps extends DepsModule {
  private lazy val redisLive: RedisClient = null
//  {
//    val redisConfig =
//      Dependencies.getGsObject(classOf[ConfDeps], classOf[Config]).redisConfig
//    RedisClient.create(
//      RedisURI.Builder
//        .redis(redisConfig.host, redisConfig.port)
//        .withPassword(redisConfig.password.toCharArray)
//        .withDatabase(redisConfig.databaseNumber)
//        .build
//    )
//  }

  private lazy val redisStringCommandLive: RedisStringCommand = null
//  {
//    new RedisStringCommand(redisLive)
//  }

  override lazy val defineObjects = List(
    classOf[RedisClient] -> redisLive,
    classOf[RedisStringCommand] -> redisStringCommandLive
  )

}
