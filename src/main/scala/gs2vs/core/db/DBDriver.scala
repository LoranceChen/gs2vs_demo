package gs2vs.core.db

import gs2vs.core.utils.DebugUtils
import gs2vs.core.utils.ExecutorUtils.GS2Executor
import org.slf4j.LoggerFactory

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util.concurrent.CompletableFuture
import scala.util.control.NonFatal

/** implement db ops for virtual thread
  */
class DBDriver(
    dataSource: DataSource,
    gs2Executor: GS2Executor
) {
  val logger = LoggerFactory.getLogger(classOf[DBDriver])

  // TODO: support sql statement pre-check
  def run[T](sql: String, rawHandler: ResultSet => T): List[T] = {
    val completableFuture = new CompletableFuture[List[T]]()

    gs2Executor.runAndWait(
      DebugUtils.timeCheck(
        "DBDriver.run inner",
        () => {
          var resultSet: List[T] = null
          try { resultSet = executeSql(sql, rawHandler) }
          catch {
            case NonFatal(e) =>
              completableFuture.completeExceptionally(e)
          }
          if (resultSet != null) {
            completableFuture.complete(resultSet)
          }
        }
      )
    )
    completableFuture.get()
  }

  def runSync[T](sql: String, rawHandler: ResultSet => T): List[T] = {
    executeSql(sql, rawHandler)
  }

  def genTransaction(): Transaction = {
    new Transaction(dataSource, gs2Executor)
  }

  // can not execute on virtual thread
  private def executeSql[T](
      sql: String,
      rawHandler: ResultSet => T
  ): List[T] = {
    var connection: Connection = null
    var pst: PreparedStatement = null
    var rs: ResultSet = null
    val dbRst = scala.collection.mutable.ArrayBuffer[T]()

    try {
      connection = dataSource.getConnection()
      pst = connection.prepareStatement(sql)
      rs = pst.executeQuery
      if (rs == null) return List.empty
      while (rs.next) {
        dbRst.addOne(rawHandler(rs))
      }
    } finally {
      if (rs != null) rs.close()
      if (pst != null) pst.close()
      if (connection != null) connection.close()
    }
    dbRst.toList
  }

}
