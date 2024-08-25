package gs2vs.core.wsclient;

public class WsPbData {
    public byte head;
    short serviceIndex;
    byte actionIndex;
    int sequence;
    byte[] pb;
}