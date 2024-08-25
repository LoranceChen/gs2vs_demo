# gs2vs (reearch)

gs2vs is short name of Game Server2 in Virtual Thread(JDK21). gs2vs will power of vanilla scala traditional grammar and
fiber-based runtime like ZIO library. Easy to create both of IO & CPU heavily game server all-in-one.

## Dependencies

- netty
- disruptor
- jdk21
- middleware client jar
    - lettuce (redis client)
    - hikari (db connection pool)

## Features (now)

- websocket service: request-response module
- virtual thread developing style

## Example to run

- setting pgsql and redis address, see `ConfDeps`
- `sbt run` start up the server
- ~~open browser: `127.0.0.1:8880`~~
- ~~input message to send websocket protocol: `UserService;signUp;my_name002`~~
- wsclient test case
    - 按照`example to run`启动服务器
    - case1: `WsPbClientTest` 通过ws+PB协议调用server接口
    - case2: `LoadTester` 包含多个测试场景（使用公共pgsql和redis时，请勿压测过大）
    - 注意: 使用logger level = debug 查看日志
- [optional] OTel support in VM options

```
  -javaagent:"C:\personal\gs2vs\external\opentelemetry-javaagent_1.33.4.jar"
  -Dotel.exporter.otlp.endpoint=http://xxx:xxx
  -Dotel.exporter.otlp.protocol=grpc
  -Dotel.service.name=gs2vs
  -Dotel.metrics.exporter=none
  -Djdk.tracePinnedThreads=full
```

## Performance

- 小批量测试：当前使用`LoadTester.doLoadTestFewClientMultiReq`测试用例模拟多客户端多请求情况。
    - Virtual thread收发层延迟：~300 us (log: server total time , multipleCall ... total time)
    - virtual thread等待OS thread延迟：100~150 us (log: dbDriver.run select, DBDriver.run inner)
- CPU高负载测试
    - TODO

## TODO LIST

- DI support
- Middleware driver support, such as: redis, db, kafka (process: 40%)
- WebSocket server-push mode
- actor module for easy to use in-memory game developing
- cluster sharding base on actors
- multiple actor transaction support
- benchmark compare with gRPC
