package gs2vs.java.core.grpclient;

import gs2vs.services.game.protobuf.GrpcServiceOuterClass;
import gs2vs.core.grpclient.AsyncClient;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class GrpcLoadTester {
    // 总个数：(rspCountPerChannel + outstandingRpcsPerChannel) * channels
    static int warmUpChannels = 1;
    static int warmUpPutstandingRpcsPerChannel = 1;
    static int warmUpRspCountPerChannel = 10_000;

    static int putstandingRpcsPerChannel = 1;
//    static int channels = 20;
    static int channels = 5;
//    static int rspCountPerChannel = 1_000_000;
//    static int rspCountPerChannel = 200_000;
    static int rspCountPerChannel = 500_000;
//    static int rspCountPerChannel = 750_000;

//    static int channels = 4;
//    static int outstandingRpcsPerChannel = 10;
//    static int rspCountPerChannel = 10_000;


    public static void main(String[] args) throws InterruptedException {
        Logger logger = LoggerFactory.getLogger(GrpcLoadTester.class);
        try {
            // warmup
            for (int i = 0; i < warmUpChannels; i++) {
                doLoadTest(logger, i, warmUpRspCountPerChannel, warmUpPutstandingRpcsPerChannel, true);
            }
            Thread.sleep(10_000);

            System.out.println("===========begin");
            for (int i = 0; i < channels; i++) {
                doLoadTest(logger, i, rspCountPerChannel, putstandingRpcsPerChannel, false);
            }

            // hannel index: 0, count: 1000001, time cost: 93140ms,
            Thread.sleep(10_000);
            System.out.println("===========end");

            // thread

//            for (int i = 0; i < channels; i++) {
//                doLoadTest(logger, i, rspCountPerChannel, outstandingRpcsPerChannel,false);
//            }
        } catch (Throwable e) {
            logger.error("e: ", e);
        }


        Thread.currentThread().join();

    }

    public static void doLoadTest(Logger logger, int i, int _rspCountPerChannel, int _outstandingRpcsPerChannel, boolean warmUp) throws InterruptedException {
        AsyncClient asyncClient = new AsyncClient();

        AtomicLong endTime = new AtomicLong(0L);
        AtomicInteger statistic = new AtomicInteger(0);

        final int _i = i;
        AtomicReference<ClientCallStreamObserver<GrpcServiceOuterClass.HelloStreamRequest>> requestBox = new AtomicReference<>();
        AtomicReference<ManagedChannel> channelBox = new AtomicReference<>();
        final AtomicLong beginTime = new AtomicLong();
        ClientCallStreamObserver<GrpcServiceOuterClass.HelloStreamRequest> request = asyncClient.start(rsp -> {
            if(warmUp) logger.info("get result: " + rsp.getEcho() + ", sequence: " + rsp.getSequence());

            //                logger.info("ready msg3: " + msg);
            if (statistic.incrementAndGet() <= _rspCountPerChannel) {
                synchronized (requestBox.get()) {
                    requestBox.get().request(1);
                    if(warmUp) logger.info("request next sequence: " + (rsp.getSequence() + 1));
                    requestBox.get().onNext(GrpcServiceOuterClass.HelloStreamRequest.newBuilder().setMsg(rsp.getEcho()).setSequence(rsp.getSequence() + 1).build());
                }
            } else {
                endTime.set(System.currentTimeMillis());
                int count = statistic.get();
                long timeCost = (endTime.get() - beginTime.get());
                logger.info("channel index: " + _i + ", count: "+ count + ", time cost: " + timeCost + "ms" + ", qps: " + (count / (timeCost/ 1000f)) );
                if(!channelBox.get().isShutdown()){
                    channelBox.get().shutdown();
                }
            }
        }, channelBox);

        requestBox.set(request);

        Thread.sleep(1000);

        beginTime.set(System.currentTimeMillis());
        // send init data
        for (int j = 0; j < _outstandingRpcsPerChannel; j++) {
            int seq = ((i+1)* 10 + (j+1)) * 1_000_000 ;
            synchronized (request) {
                request.onNext(GrpcServiceOuterClass.HelloStreamRequest.newBuilder().setSequence(seq).setMsg("message:"+seq).build());
            }
        }
    }

}
