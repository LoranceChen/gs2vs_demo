package gs2vs.core.grpcsvr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.*;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

public class GrpcServer {
    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "9881" : "9880"));

    public Server newServer(BindableService service) throws IOException {
        final EventLoopGroup boss;
        final EventLoopGroup worker;
        final Class<? extends ServerChannel> channelType;
        ThreadFactory tf = new DefaultThreadFactory("server-elg-", true /*daemon */);

        boss = new  NioEventLoopGroup(1, tf);
        worker = new NioEventLoopGroup(tf);
        channelType = NioServerSocketChannel.class;
        SocketAddress sockaddr = new InetSocketAddress("127.0.0.1", PORT);
        NettyServerBuilder builder = NettyServerBuilder
                .forAddress(sockaddr)
                .permitKeepAliveTime(2, TimeUnit.SECONDS)
                .keepAliveTime(5, TimeUnit.SECONDS)
                .keepAliveTimeout(18, TimeUnit.SECONDS)
                .permitKeepAliveWithoutCalls(true)
                .bossEventLoopGroup(boss)
                .workerEventLoopGroup(worker)
                .channelType(channelType)
                .addService(service)
                .executor(Executors.newWorkStealingPool())
                .maxInboundMessageSize(4 * 1024 * 1024)
                .maxInboundMetadataSize(4 * 1024 * 1024);
//                .flowControlWindow(1024);

            // TODO(carl-mastrangelo): This should not be necessary.  I don't know where this should be
            // put.  Move it somewhere else, or remove it if no longer necessary.
            // See: https://github.com/grpc/grpc-java/issues/2119
//        builder.executor(new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
//                new ForkJoinPool.ForkJoinWorkerThreadFactory() {
//                    final AtomicInteger num = new AtomicInteger();
//                    @Override
//                    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
//                        ForkJoinWorkerThread thread =
//                                ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
//                        thread.setDaemon(true);
//                        thread.setName("grpc-server-app-" + "-" + num.getAndIncrement());
//                        return thread;
//                    }
//                }, UncaughtExceptionHandlers.systemExit(), true /* async */));

        System.out.println("grpc server start at port: " + PORT);

        return builder.build().start();
    }

}
