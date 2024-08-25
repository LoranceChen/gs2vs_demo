/*
 * Copyright 2012 The Netty Project
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
package gs2vs.core.wsserver.gsjava;

import gs2vs.core.data.ObjectId;
import gs2vs.core.diaptcher.Dispatcher;
import gs2vs.core.diaptcher.InputMsg;
import gs2vs.core.wsserver.WSocketManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * Echoes uppercase content of text frames.
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    String wsId = null;
    int wsIdHashCode;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        wsId = new ObjectId().toString();
        wsIdHashCode = wsId.hashCode();
        System.out.println("WebSocketFrameHandler handlerAdded. wsId: " + wsId);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        System.out.println("WebSocketFrameHandler handlerRemoved. wsId: " + wsId);
        WSocketManager.concurrentHashMap().remove(wsId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof BinaryWebSocketFrame) {
            long beginTime = System.nanoTime();
//            System.out.println("begin time: " + beginTime);

            // Send the uppercase string back.
            ByteBuf content = frame.content();
            if (!content.isReadable()) return; // skip empty proto

            byte head = content.readByte(); // using extension

            short service = content.readShortLE();
            byte action = content.readByte();
            int sequence = content.readIntLE();

            // get all left as pb
            byte[] pb = new byte[content.readableBytes()];
            content.readBytes(pb);

            WSocketManager.concurrentHashMap().putIfAbsent(wsId, ctx.channel());

            InputMsg inputMsg = new InputMsg();
//            inputMsg.setMsg(methodName);
            inputMsg.setServiceIndex(service);
            inputMsg.setActionIndex(action);
            inputMsg.setSequence(sequence);
            inputMsg.setMsgPB(pb);
            inputMsg.setEntityHashId(wsId.hashCode()); // only calc once
            inputMsg.setWsId(wsId);
            inputMsg.setTimeNano(beginTime);
            Dispatcher.inst().produce(inputMsg);
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // todo first connect do somthing

//        if(evt instanceof WebSocketServerProtocolHandler.)
//        else
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            //Channel upgrade to websocket, remove WebSocketIndexPageHandler.
            ctx.pipeline().remove(WebSocketIndexPageHandler.class);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
