package gs2vs.core

case class Config(
    jdbcConfig: JdbcConfig,
    redisConfig: RedisConfig,
    pyroscopeConfig: PyroScopeConfig
)

case class JdbcConfig(
    url: String,
    username: String,
    password: Option[String],
    threadPoolSize: Int
)

case class RedisConfig(
    host: String,
    port: Int,
    password: String,
    databaseNumber: Int
)

case class PyroScopeConfig(
    serverUrl: String,
    serverKey: String
)
