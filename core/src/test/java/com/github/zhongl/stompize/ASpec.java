package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class ASpec extends Specification {

    private final FrameVisitor visitor;

    protected ASpec(FrameVisitor visitor, boolean z, byte b, char c, short s, int i, float f, long l, double d) {
        this.visitor = visitor;
    }

    public abstract void send(
            @Required("destination") String destination,
            @Optional("receipt") String receipt,
            Object content
    );

    public void receipt(@Optional("receipt-id") String id) {
        System.out.println("received receipt: " + id);
    }

    @Override
    protected FrameVisitor output() {
        return visitor;
    }

}
