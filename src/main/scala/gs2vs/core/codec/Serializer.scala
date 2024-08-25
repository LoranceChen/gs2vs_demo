package gs2vs.core.codec
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.msgpack.jackson.dataformat.MessagePackFactory

/** generate code at runtime
  */
object Serializer {
  // demo for using
  def main(args: Array[String]): Unit = {
    val state = MyState(
      userId = "",
      kingdom = Kingdom(
        "kingdom001",
        Placements(Set(Place((1.0f, 2.0f), (1001, "pc1001"))))
      ),
      items = Item(1002, 10) :: Nil
    )
    val bytes = getBytes(state)
    println("bytes: " + bytes)
//    val obj = getObject[MyState2](bytes, classOf[MyState2])
    val obj = getObject[MyState3](bytes, classOf[MyState3])
    println("obj: " + obj)

    val obj2 = getJsonString(obj)
    println("obj2: " + obj2)

  }
  val mapper = new ObjectMapper(new MessagePackFactory())
  mapper.registerModule(DefaultScalaModule)

  val mapperJson = new ObjectMapper()
  mapperJson
    .registerModule(DefaultScalaModule)
    .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

  def getBytes(obj: Object): Array[Byte] = {
    mapper.writeValueAsBytes(obj)
  }

  def getObject[T](bytes: Array[Byte], clazz: Class[T]): T = {
    mapper.readValue(bytes, clazz)
  }

  def getJsonString(obj: Object): String = {
    mapperJson.writeValueAsString(obj)
  }

}

case class MyState(userId: String, kingdom: Kingdom, items: List[Item])
case class MyState2(userId: String, items: List[Item])
case class MyState3(
    userId: String,
    kingdom: Kingdom,
    items: List[Item],
    items2: List[Item],
    kingdom2: Kingdom = Kingdom("", Placements(Set.empty))
)

case class Kingdom(name: String, placements: Placements) {
  def this() = {
    this("", Placements(Set.empty))
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case Kingdom(name, placements) => if (name == "") true else false
      case _                         => false
    }
  }
}

case class Placements(place: Set[Place]) {
  def this() = {
    this(Set.empty)
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case Placements(placements) => if (placements.isEmpty) true else false
      case _                      => false
    }
  }
}
case class Place(position: (Float, Float), structure: (Int, String))
case class Item(dataId: Int, amount: Int)
