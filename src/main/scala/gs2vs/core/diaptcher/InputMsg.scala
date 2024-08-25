package gs2vs.core.diaptcher

class InputMsg {
  var wsId: String = _
  var entityHashId: Int = _
  var serviceIndex: Short = _
  var actionIndex: Byte = _
  var sequence: Int = _
  var msgPB: Array[Byte] = _
  var timeNano: Long = _

  def setServiceIndex(serviceIndex: Short) = this.serviceIndex = serviceIndex
  def setActionIndex(methodIndex: Byte) = this.actionIndex = methodIndex
  def setSequence(sequence: Int) = this.sequence = sequence
  def setMsgPB(msgPB: Array[Byte]) = this.msgPB = msgPB
  def setWsId(wsId: String) = this.wsId = wsId
  def setEntityHashId(entityHashId: Int) = this.entityHashId = entityHashId
  def setTimeNano(nanoTime: Long) = this.timeNano = nanoTime

  def copyFrom(other: InputMsg): Unit = {
    this.wsId = other.wsId
    this.entityHashId = other.entityHashId
//    this.entityType = other.entityType
//    this.shardingStreamId = other.shardingStreamId
//    this.requestId = other.requestId
//    this.msgType = other.msgType
    this.serviceIndex = other.serviceIndex
    this.actionIndex = other.actionIndex
    this.sequence = other.sequence
    this.msgPB = other.msgPB
    this.timeNano = other.timeNano

//    this.timestamp = other.timestamp
  }
}
