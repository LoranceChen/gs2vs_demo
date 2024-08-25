package gs2vs.java.core.wsclient.client;

import com.google.protobuf.InvalidProtocolBufferException;
import gs2vs.services.game.protobuf.UserService;
import gs2vs.core.exception.CheckedException;
import gs2vs.core.wsclient.WebSocketClient;
import gs2vs.core.wsclient.WsPbClient;
import gs2vs.protoindex.ServiceProtoConst;
import gs2vs.protoindex.UserServiceProto;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoadTester {
    public static void main(String[] args) throws InterruptedException {

//        doLoadTestOneReqPerClient();
//        doLoadTestOneClientMultiReq();
//        doLoadTestFewClientMultiReq();
//        doLoadTestOneClientMultiReq2();
        doLoadTestMultipleClientMultiReq2();
        Thread.currentThread().join();
    }

    private static void doLoadTestOneReqPerClient() {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < 10; i++) {
            executorService.execute(LoadTester::doLoadTestOneReqPerClient);
        }

        System.out.println("==========load test======");
        for (int i = 0; i < 100; i++) {
            executorService.execute(LoadTester::doLoadTestOneReqPerClient);
        }
    }

    private static void oneReqPerClientAction() {
        WebSocketClient webSocketClient = new WebSocketClient();
        try {
            webSocketClient.connect();
        } catch (Exception e) {
            throw new CheckedException(e);
        }

        WsPbClient wsPbClient = new WsPbClient(webSocketClient);
        var msg = UserService.MultipleCallRequest.newBuilder().build();
        UserService.MultipleCallResponse response = wsPbClient.sendFrame(
                ServiceProtoConst.USER_SERVICE(), UserServiceProto.MULTIPLE_CALL(),
                msg,
                proto -> {
                    try {
                        return UserService.MultipleCallResponse.parseFrom(proto);
                    } catch (InvalidProtocolBufferException e) {
                        throw new CheckedException(e);
                    }
                });
    }

    private static void doLoadTestOneClientMultiReq() {
        WebSocketClient webSocketClient = new WebSocketClient();
        try {
            webSocketClient.connect();
        } catch (Exception e) {
            throw new CheckedException(e);
        }

        WsPbClient wsPbClient = new WsPbClient(webSocketClient);

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < 10; i++) {
//            executorService.execute(() -> LoadTester.oneClientMultiReqAction(wsPbClient));
            LoadTester.oneClientMultiReqAction(wsPbClient);
        }

        System.out.println("==========load test======");
        for (int i = 0; i < 100; i++) {
//            executorService.execute(() -> LoadTester.oneClientMultiReqAction(wsPbClient));
            LoadTester.oneClientMultiReqAction(wsPbClient);
        }
    }

    private static void doLoadTestOneClientMultiReq2() {
        WebSocketClient webSocketClient = new WebSocketClient();
        try {
            webSocketClient.connect();
        } catch (Exception e) {
            throw new CheckedException(e);
        }

        WsPbClient wsPbClient = new WsPbClient(webSocketClient);

//        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < 20000; i++) {
//            executorService.execute(() -> LoadTester.oneClientMultiReqAction(wsPbClient));
            LoadTester.oneClientMultiReqAction2(wsPbClient,i);
        }

        System.out.println("==========load test======");
        long beginTime = System.currentTimeMillis();
//        var COUNTS = 1_000_000;
//        var COUNTS = 500_000;
        var COUNTS = 750_000;
        for (int i = 0; i < COUNTS; i++) {
//            executorService.execute(() -> LoadTester.oneClientMultiReqAction(wsPbClient));
            LoadTester.oneClientMultiReqAction2(wsPbClient,i);
        }
        long endTime = System.currentTimeMillis();

        // request: 1000k, time: 132.925s, qps: 7523
        System.out.println(String.format("==== get result,COUNTS: %d, time cost: %d ms, qps: %s", COUNTS, endTime - beginTime, COUNTS / ((endTime - beginTime) / 1000f )));
    }


    private static void doLoadTestMultipleClientMultiReq2() {
        WebSocketClient webSocketClient = new WebSocketClient();
        try {
            webSocketClient.connect();
        } catch (Exception e) {
            throw new CheckedException(e);
        }

        WsPbClient wsPbClient = new WsPbClient(webSocketClient);

        for (int i = 0; i < 20000; i++) {
//            executorService.execute(() -> LoadTester.oneClientMultiReqAction(wsPbClient));
            LoadTester.oneClientMultiReqAction2(wsPbClient,i);
        }

        System.out.println("==========load test======");
        long beginTime = System.currentTimeMillis();

//        var COUNTS = 200_000;
        var COUNTS = 500_000;
//        var COUNTS = 750_000;
        var CONNECTIONS = 5;
//        var COUNTS = 1_000_000;

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        var rstA = new ArrayList<Future>(CONNECTIONS);
        for (int _i = 0; _i < CONNECTIONS; _i++) {
            var rst = executorService.submit(() -> {
                WebSocketClient _webSocketClient = new WebSocketClient();
                try {
                    _webSocketClient.connect();
                } catch (Exception e) {
                    throw new CheckedException(e);
                }

                WsPbClient _wsPbClient = new WsPbClient(_webSocketClient);

                for (int i = 0; i < COUNTS; i++) {
                    LoadTester.oneClientMultiReqAction2(_wsPbClient,i);
                }}
            );
            rstA.add(rst);
        }

        rstA.forEach(item -> {
            try {
                item.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        long endTime = System.currentTimeMillis();

        // 1 connection, request: 100w, time: 132.925s, qps: 7523
        // COUNTS: 750000, CONNECTIONS: 20, time cost: 417216 ms, qps: 35952.6, cpu: 500%
        System.out.println(String.format("==== get result,COUNTS: %d, CONNECTIONS: %d, time cost: %d ms, qps: %s", COUNTS, CONNECTIONS, endTime - beginTime, COUNTS * CONNECTIONS / ((endTime - beginTime) / 1000f )));
    }

    private static void doLoadTestFewClientMultiReq() {

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < 5; i++) {
            executorService.execute(() -> {
                WebSocketClient webSocketClient = new WebSocketClient();
                try {
                    webSocketClient.connect();
                } catch (Exception e) {
                    throw new CheckedException(e);
                }
                WsPbClient wsPbClient = new WsPbClient(webSocketClient);
                for (int i2 = 0; i2 < 10; i2++) {
                    LoadTester.oneClientMultiReqAction(wsPbClient);
                }
            });
        }


        System.out.println("==========load test======");
        for (int i = 0; i < 5; i++) {
            executorService.execute(() -> {
                WebSocketClient webSocketClient = new WebSocketClient();
                try {
                    webSocketClient.connect();
                } catch (Exception e) {
                    throw new CheckedException(e);
                }
                WsPbClient wsPbClient = new WsPbClient(webSocketClient);
                for (int i2 = 0; i2 < 100; i2++) {
                    LoadTester.oneClientMultiReqAction(wsPbClient);
                }
            });
        }

    }

    private static void oneClientMultiReqAction(WsPbClient wsPbClient) {
        var msg = UserService.MultipleCallRequest.newBuilder().build();
        UserService.MultipleCallResponse response = wsPbClient.sendFrame(
                ServiceProtoConst.USER_SERVICE(), UserServiceProto.MULTIPLE_CALL(),
                msg,
                proto -> {
                    try {
                        return UserService.MultipleCallResponse.parseFrom(proto);
                    } catch (InvalidProtocolBufferException e) {
                        throw new CheckedException(e);
                    }
                });
    }

    private static void oneClientMultiReqAction2(WsPbClient wsPbClient, int index) {
        var msg = UserService.HelloRequest.newBuilder().setMsg("message:"+index).setSequence(index+1).build();
        UserService.HelloResponse response = wsPbClient.sendFrame(
                ServiceProtoConst.USER_SERVICE(), UserServiceProto.HELLO(),
                msg,
                proto -> {
                    try {
                        return UserService.HelloResponse.parseFrom(proto);
                    } catch (InvalidProtocolBufferException e) {
                        throw new CheckedException(e);
                    }
                });
    }


}
