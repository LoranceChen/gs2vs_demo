package gs2vs.dependency
import gs2vs.core.db.DataSource
import gs2vs.core.{Config, Dependencies, JdbcConfig}
import gs2vs.wsservice.UserService

import java.sql.Connection

/** support mock deps
  */
object DepsMockTest {
  def main(args: Array[String]): Unit = {
    testConfigMock()
  }

  def testConfigMock(): Unit = {
    val newConfig: Config = Config(
      jdbcConfig = JdbcConfig(
        "jdbc:postgresql://localhost/test",
        "lorance",
        None,
        100111
      ),
      null,
      null
    )
    Dependencies.init
    Dependencies.mockPrepare(
      classOf[DBDeps],
      new DataSource { override def getConnection(): Connection = null }
    )
    Dependencies.mockPrepare(classOf[ConfDeps], newConfig)
    Dependencies.start
    val getObj =
      Dependencies.getGsObject(classOf[ConfDeps], classOf[Config])
    println(s"with mocked Obj: ${getObj}")

  }

  def testServiceMock(): Unit = {
    val userService: UserService = new UserService(null, null)
    Dependencies.init
    Dependencies.mockPrepare(classOf[ServiceDeps], userService)
    Dependencies.start
    val getObj =
      Dependencies.getGsObject(classOf[ServiceDeps], classOf[UserService])
    println(s"with mocked Obj: ${getObj}")

  }

}
