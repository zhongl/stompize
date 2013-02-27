package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class StompClient extends Specification implements Stomp {

    private final FrameOutput output;

    protected StompClient(FrameOutput output, boolean z, byte b, char c, short s, int i, float f, long l, double d) {
        this.output = output;
    }

    @Override
    public void receipt(@Optional("receipt-id") String id) {
        System.out.println("received receipt: " + id);
    }

    @Override
    protected FrameOutput output() {
        return output;
    }

}
