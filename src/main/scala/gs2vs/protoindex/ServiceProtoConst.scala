package gs2vs.protoindex

object ServiceProtoConst {
  val USER_SERVICE: Short = 1
  val PGSQL_SERVICE: Short = 2

  val namedService: Map[Short, String] = Map(
    USER_SERVICE -> "USER_SERVICE",
    PGSQL_SERVICE -> "PGSQL_SERVICE"
  )

}
