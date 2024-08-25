/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gs2vs.core.grpclient;

import gs2vs.services.game.protobuf.GrpcServiceGrpc;
import gs2vs.services.game.protobuf.GrpcServiceOuterClass;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * QPS Client using the non-blocking API.
 */
public class AsyncClient {
    Logger logger = LoggerFactory.getLogger(this.getClass());
  /**
   * Start the QPS Client.
   */
  public ClientCallStreamObserver<GrpcServiceOuterClass.HelloStreamRequest> start(Consumer<GrpcServiceOuterClass.HelloStreamResponse> onRsp, AtomicReference<ManagedChannel> channelRef) {
      ManagedChannel channel = ManagedChannelBuilder
              .forAddress("127.0.0.1", 9880)
              .keepAliveTime(5, TimeUnit.SECONDS)
              .keepAliveTimeout(18, TimeUnit.SECONDS)
              .keepAliveWithoutCalls(true)
              .usePlaintext()
              .maxInboundMessageSize(4 * 1024 * 1024)
              .maxInboundMetadataSize(4 * 1024 * 1024)
              .executor(Executors.newWorkStealingPool())
              .build();

      channel.notifyWhenStateChanged(ConnectivityState.CONNECTING, () -> {
          logger.info("asyncclient notifyWhenStateChanged CONNECTING to server");
      });
      channel.notifyWhenStateChanged(ConnectivityState.IDLE, () -> {
          logger.info("asyncclient notifyWhenStateChanged IDLE");
      });
      channel.notifyWhenStateChanged(ConnectivityState.READY, () -> {
          logger.info("asyncclient notifyWhenStateChanged READY");
      });
      channel.notifyWhenStateChanged(ConnectivityState.SHUTDOWN, () -> {
          logger.info("asyncclient notifyWhenStateChanged SHUTDOWN");
      });
      channel.notifyWhenStateChanged(ConnectivityState.TRANSIENT_FAILURE, () -> {
          logger.info("asyncclient notifyWhenStateChanged TRANSIENT_FAILURE");
      });

      GrpcServiceGrpc.GrpcServiceStub grpcServiceStub = GrpcServiceGrpc.newStub(channel);

      StreamObserver<GrpcServiceOuterClass.HelloStreamResponse> responseObserver =
              new StreamObserver<GrpcServiceOuterClass.HelloStreamResponse>(){

                  @Override
                  public void onNext(GrpcServiceOuterClass.HelloStreamResponse value) {
                        onRsp.accept(value);
                  }

                  @Override
                  public void onError(Throwable t) {
                      logger.error("asyncclient error", t);
                  }

                  @Override
                  public void onCompleted() {
                      logger.info("asyncclient completed");

                  }
              };
      StreamObserver<GrpcServiceOuterClass.HelloStreamRequest> streamObserver = grpcServiceStub.helloStream(responseObserver);

      channelRef.set(channel);
      return (ClientCallStreamObserver<GrpcServiceOuterClass.HelloStreamRequest>) streamObserver;
  }

  private static void shutdown(List<ManagedChannel> channels) {
    for (ManagedChannel channel : channels) {
      channel.shutdown();
    }
  }

}
