package gs2vs.core.diaptcher

class OutputMsg {
  var wsId: String = _
  var entityHashId: Int = _
  var head: Byte = _
  var serviceIndex: Short = _
  var actionIndex: Byte = _
  var sequence: Int = _
  var msgPb: Array[Byte] = _
  var failPb: Array[Byte] = _
  var beginTimeNano: Option[Long] = _

  def copyFrom(other: OutputMsg): Unit = {
    this.wsId = other.wsId
    this.entityHashId = other.entityHashId
    this.head = other.head
    this.serviceIndex = other.serviceIndex
    this.actionIndex = other.actionIndex
    this.sequence = other.sequence
    this.msgPb = other.msgPb
    this.failPb = other.failPb
    this.beginTimeNano = other.beginTimeNano
  }

}
