package gs2vs.wsservice

import gs2vs.core.db.DBDriver
import gs2vs.core.diaptcher.InputEventConsumer
import gs2vs.core.wsservice.Action.{ActionHandler, ActionIndex}
import org.slf4j.LoggerFactory

@deprecated("", "")
class PgSqlServiceTest(dbDriver: DBDriver) {
  protected val logger =
    LoggerFactory.getLogger(classOf[InputEventConsumer].getName)

  val actions: Map[ActionIndex, ActionHandler] = {
    Map[ActionIndex, ActionHandler](
//      "createUsersTable" -> createUsersTable,
//      "selectUserNameById" -> selectUserNameById
    )
  }

  def createUsersTable(param: Object): Object = {
    dbDriver
      .run[Unit]("create table Users(id int, name varchar(256));", _ => ())
  }

  def selectUserNameById(id: Object): Option[String] = {
    dbDriver
      .run[String](
        s"select name from Users where id = ${id}",
        rs => rs.getString("name")
      )
      .headOption
  }

}
