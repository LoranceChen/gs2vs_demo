package gs2vs.wsservice

import gs2vs.services.game.protobuf.PgsqlService.{
  SelectUserNameByIdRequest,
  SelectUserNameByIdResponse
}
import gs2vs.core.db.DBDriver
import gs2vs.core.wsservice.AService
import gs2vs.core.wsservice.Action.{ActionHandler, ActionIndex}
import gs2vs.protoindex.PgSqlServiceProto

class PgSqlService(dbDriver: DBDriver) extends AService with ProtoJsonUtils {
  override val serviceName: String = "PgSqlService"

  override val actions: Map[ActionIndex, ActionHandler] = {
    Map[ActionIndex, ActionHandler](
//      PgSqlServiceProto.CREATE_USERS_TABLE -> createUsersTable,
      PgSqlServiceProto.SELECT_USER_NAME_BY_ID -> { data =>
        this.handleClientLog(
          "SELECT_USER_NAME_BY_ID",
          SelectUserNameByIdRequest.parseFrom(data),
          selectUserNameById
        )
      }
    )
  }

  def createUsersTable(param: Object): Object = {
    dbDriver
      .run[Unit]("create table Users(id int, name varchar(256));", _ => ())
  }

  def selectUserNameById(
      request: SelectUserNameByIdRequest
  ): SelectUserNameByIdResponse = {
    val rst = dbDriver
      .run[String](
        s"select name from Users where id = ${request.getId.toInt}",
        rs => rs.getString("name")
      )

    SelectUserNameByIdResponse.newBuilder().setName(rst.head).build()
  }

}
