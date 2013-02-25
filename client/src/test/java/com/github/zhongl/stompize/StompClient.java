package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class StompClient implements StompV1_2 {

    protected StompClient(Callback callback) {this.callback = callback;}

    @Override
    public void connected(
            @Required("version") String version,
            @Optional("session") String session,
            @Optional("server") String server,
            @Optional("heart-beat") String heartBeat
    ) { /*NOOP*/ }

    @Override
    public void receipt(@Required("receipt-id") String receiptId) {
        System.out.println("Get a receipt: " + receiptId);
    }

    @Override
    public void error(@Optional("message") String message, Content content) {
        System.err.println("Get an error: " + message);
    }

    @Override
    public void message(
            @Required("destination") String destination,
            @Required("message-id") String messageId,
            @Required("subscription") String subscription,
            @Optional("ack") String ack,
            Content content
    ) {
        if (callback == null) {
            System.err.println("Unexpected message received, because there is no callback for it.");
            disconnect(null);
            return;
        }
        callback.receive(content);
        ack(ack == null ? messageId : ack, null, null);
    }

    private final Callback callback;
}
