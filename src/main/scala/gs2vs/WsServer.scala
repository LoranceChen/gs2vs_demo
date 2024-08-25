package gs2vs

import gs2vs.core.httpdemo.HttpHelloWorldServer
import gs2vs.core.wsserver.gsjava.WebSocketServer

class WsServer {
  def start(): Unit = {
    println("start WsNettyServer")
    WebSocketServer.run()
    HttpHelloWorldServer.main()
  }
}
