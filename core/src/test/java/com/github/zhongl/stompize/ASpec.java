package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class ASpec extends Specification implements Stomp {

    private final FrameVisitor visitor;

    protected ASpec(FrameVisitor visitor, boolean z, byte b, char c, short s, int i, float f, long l, double d) {
        this.visitor = visitor;
    }

    @Override
    public void receipt(@Optional("receipt-id") String id) {
        System.out.println("received receipt: " + id);
    }

    @Override
    protected FrameVisitor output() {
        return visitor;
    }

}
