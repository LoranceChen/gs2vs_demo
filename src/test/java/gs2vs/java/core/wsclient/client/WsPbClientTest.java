package gs2vs.java.core.wsclient.client;

import com.google.protobuf.InvalidProtocolBufferException;
import gs2vs.services.game.protobuf.UserService;
import gs2vs.core.exception.CheckedException;
import gs2vs.core.wsclient.WebSocketClient;
import gs2vs.core.wsclient.WsPbClient;
import gs2vs.protoindex.ServiceProtoConst;
import gs2vs.protoindex.UserServiceProto;

public class WsPbClientTest {
    public static void main(String[] args) throws Exception {
        WebSocketClient webSocketClient = new WebSocketClient();
        webSocketClient.connect();
        WsPbClient wsPbClient = new WsPbClient(webSocketClient);

        var msg = UserService.MultipleCallRequest.newBuilder().build();
//        PgsqlService.InnerStruct innerStruct = PgsqlService.InnerStruct.newBuilder().setAge(11111).build();
//        var msg = PgsqlService.SelectUserNameByIdRequest.newBuilder().setId("1").setEmptyField("empty field 001").setInnerStruct(innerStruct).build();
        System.out.println("get start: ");

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

        System.out.println("get response with RPC mode: " + response);
    }
}
