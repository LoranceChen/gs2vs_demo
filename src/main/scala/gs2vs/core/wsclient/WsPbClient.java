/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gs2vs.core.wsclient;

import com.google.protobuf.MessageLite;
import gs2vs.core.exception.CheckedException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

public final class WsPbClient {
    WebSocketClient webSocketClient;
    int sequence = 0;
    ConcurrentHashMap<Integer, CompletableFuture<WsPbData>> responses = new ConcurrentHashMap<>();
    Lock sendLock = new ReentrantLock();

    private WsPbClient() {

    }

    public WsPbClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
        this.registerResponseMatcher();
    }

    public void sendFrame(short serviceIndex, byte actionIndex, MessageLite pbMsg) {
        try {
            sendLock.lock();
            byte[] pb = pbMsg.toByteArray();
            var buf = Unpooled.buffer(1 + 3 + 4 + pb.length);
            buf.writeByte(0); // placeholder
            buf.writeShortLE(serviceIndex);
            buf.writeByte(actionIndex);
            buf.writeIntLE(++sequence); // sequence
            buf.writeBytes(pb); // pb
            WebSocketFrame frame = new BinaryWebSocketFrame(buf);
            webSocketClient.sendFrame(frame);
        } finally {
            sendLock.unlock();
        }

    }

    public <T extends MessageLite> T sendFrame(short serviceIndex, byte actionIndex,
                                               MessageLite pbMsg, Function<byte[], T> pbMsgExtractor) {
        CompletableFuture<WsPbData> asyncResponse = new CompletableFuture<>();

        try {
            sendLock.lock();
            sequence += 1;
            responses.put(sequence, asyncResponse);
            byte[] pb = pbMsg.toByteArray();
            var buf = Unpooled.buffer(1 + 3 + 4 + pb.length);
            buf.writeByte(0); // placeholder
            buf.writeShortLE(serviceIndex);
            buf.writeByte(actionIndex);
            buf.writeIntLE(sequence); // sequence
            buf.writeBytes(pb); // pb
            WebSocketFrame frame = new BinaryWebSocketFrame(buf);
            webSocketClient.sendFrame(frame);
        } finally {
            sendLock.unlock();
        }

        try {
            return pbMsgExtractor.apply(asyncResponse.get().pb);
        } catch (InterruptedException | ExecutionException e) {
            throw new CheckedException(e);
        }

    }

    private void onReceive(Consumer<WsPbData> handlePbMsg) {
        this.webSocketClient.handler.registerOnReceiveHandler(frame -> {
            ByteBuf content = frame.content();
            WsPbData wsPbData = new WsPbData();
            wsPbData.head = content.readByte();
            wsPbData.serviceIndex = content.readShortLE();
            wsPbData.actionIndex = content.readByte();
            wsPbData.sequence = content.readIntLE();
            int pbSize = content.readableBytes();
            var pb = new byte[pbSize];
            content.readBytes(pb);
            wsPbData.pb = pb;

            handlePbMsg.accept(wsPbData);
        });
    }

    private void registerResponseMatcher() {
        this.onReceive(wsPbData -> {
            CompletableFuture<WsPbData> asyncResponse = responses.get(wsPbData.sequence);
            if (asyncResponse != null) {
                asyncResponse.complete(wsPbData);
            }
        });
    }

}
