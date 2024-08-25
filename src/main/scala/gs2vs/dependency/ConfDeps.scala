package gs2vs.dependency
import gs2vs.DepsModule
import gs2vs.core.{Config, JdbcConfig, PyroScopeConfig, RedisConfig}

class ConfDeps extends DepsModule {
  private lazy val configLive: Config = Config(
    jdbcConfig = JdbcConfig(
      "jdbc:postgresql://localhost/gs2vs",
      "lorance",
      Some("111111"),
      20
    ),
    redisConfig = RedisConfig(
      host = "your_redis",
      port = 6380,
      password = "xxxxx",
      databaseNumber = 101
    ),
    pyroscopeConfig = PyroScopeConfig(
      serverUrl = "http://xxxx",
      serverKey = ""
    )
  )

  override protected lazy val defineObjects: List[(Class[_], Any)] = List(
    classOf[Config] -> configLive
  )

}
