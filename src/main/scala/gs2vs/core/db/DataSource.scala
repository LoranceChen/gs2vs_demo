package gs2vs.core.db

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import gs2vs.core.JdbcConfig
import org.slf4j.{Logger, LoggerFactory}

import java.sql.Connection

trait DataSource {
  def getConnection(): Connection
}

class DataSourceImpl(jdbcConfig: JdbcConfig) extends DataSource {
  protected val logger: Logger = LoggerFactory.getLogger(classOf[DataSource])

  private val hikariConfig = new HikariConfig()
  hikariConfig.setJdbcUrl(jdbcConfig.url)
  hikariConfig.setUsername(jdbcConfig.username)
  jdbcConfig.password.foreach(pwd => hikariConfig.setPassword(pwd))
  hikariConfig.setConnectionTimeout(3000)
  hikariConfig.setConnectionInitSql("select 1")
  hikariConfig.setInitializationFailTimeout(3000)
  hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
  hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
  hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

  private lazy val ds =
    try { new HikariDataSource(hikariConfig) }
    catch {
      case e: Throwable =>
//        logger.error(s"init datasource fail. jdbcConfig: ${jdbcConfig}", e)
        throw e
    }

  def getConnection(): Connection = {
    ds.getConnection()
  }

}
