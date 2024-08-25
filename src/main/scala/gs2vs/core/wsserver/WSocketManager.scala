package gs2vs.core.wsserver

import io.netty.channel.Channel

import java.util.concurrent.ConcurrentHashMap

object WSocketManager {
  val concurrentHashMap = new ConcurrentHashMap[String, Channel]()
}
