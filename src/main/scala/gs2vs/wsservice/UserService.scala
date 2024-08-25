package gs2vs.wsservice
import gs2vs.services.game.protobuf.CommonData.Kingdom
import gs2vs.services.game.protobuf.UserService.{HelloRequest, HelloResponse, MultipleCallRequest, MultipleCallResponse, SignUpRequest, SignUpResponse}
import gs2vs.core.db.DBDriver
import gs2vs.core.redis.RedisStringCommand
import gs2vs.core.utils.DebugUtils
import gs2vs.core.wsserver.WsContext
import gs2vs.core.wsservice.AService
import gs2vs.core.wsservice.Action.{ActionHandler, ActionIndex}
import gs2vs.protoindex.UserServiceProto
import org.slf4j.LoggerFactory

class UserService(
    dbDriver: DBDriver,
    redisStringCommand: RedisStringCommand
) extends AService {
  protected val logger =
    LoggerFactory.getLogger(classOf[UserService].getName)

  override val actions: Map[ActionIndex, ActionHandler] = {
    Map[Byte, ActionHandler](
      UserServiceProto.SIGN_UP -> { req =>
        signUpWithPb(SignUpRequest.parseFrom(req))
      },
      UserServiceProto.MULTIPLE_CALL -> { req =>
        multipleCall(MultipleCallRequest.parseFrom(req))
      },
      UserServiceProto.HELLO -> { req =>
        hello(HelloRequest.parseFrom(req))
      }
    )
  }

  def signUpWithPb(request: SignUpRequest): SignUpResponse = {
    logger.info(s"get request: $request")
    Thread.sleep(3000)

    // todo redis/db/etc with blocking style

    val rst = s"handled $request in UserService"
    logger.info(s"response: $rst")

    SignUpResponse
      .newBuilder()
      .setKingdom(
        Kingdom.newBuilder().setId(11111L)
      )
      .build()
  }

  def addString(): String = {
    var str = ""
    1 to 10000 foreach(i => str += i.toString)
    str
  }

  /** call redis
    */
  def multipleCall(request: MultipleCallRequest): MultipleCallResponse = {
    val begin = System.nanoTime()
    val rst = addString()
    val redisSetRst = DebugUtils.timeCheck(
      "redisStringCommand.set",
      () => redisStringCommand.set("gs2vs_multipleCall", "gs2vs_value001")
    )
    val redisGetRst = DebugUtils.timeCheck(
      "redisStringCommand.get",
      () => redisStringCommand.get("gs2vs_multipleCall")
    )

    val selectDbRst = DebugUtils.timeCheck(
      "dbDriver.run select",
      () =>
        dbDriver
          .run[String](
            s"select name from Users where id = 1",
            rs => rs.getString("name")
          )
    )
    innerCall("innercall test idea debug")

    val response = MultipleCallResponse
      .newBuilder()
      .setTimeCost((System.nanoTime() - begin).toInt)
      .build()
    doSleep()
    val context = WsContext.getUser()
    logger.info(
      s"context: [${context.getMsgId}, ${context.requestTime}] multipleCall redis get/set: $redisSetRst, " +
        s"$redisGetRst, db: $selectDbRst, total time: ${redisGetRst._2 + redisSetRst._2 + selectDbRst._2}, rst: ${rst.take(10)}, simpleOps: ${doSimpleOps}"
    )
    response
  }

  private def innerCall(string: String): String = {
    val a = 1
    val b = a * 2
    string + b.toString
  }

  def hello(request: HelloRequest): HelloResponse = {
//    if(request.getSequence == 1 || request.getSequence == 10000) logger.info(s"hello: ${request.getSequence}")
    HelloResponse.newBuilder().setEcho(request.getMsg).setSequence(request.getSequence).build()
  }


  private def doSleep(): Unit = {
    Thread.sleep(500)
  }

  private def doSimpleOps(): Int = {
    1+1
  }


}
