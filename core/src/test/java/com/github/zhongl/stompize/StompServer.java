package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class StompServer extends Specification implements Stomp {

    protected StompServer(FrameOutput output) {this.output = output;}

    @Override
    protected FrameOutput output() {
        return output;
    }

    @Override
    public void send(@Required("destination") String destination, @Optional("receipt") String receipt, Object content) {
        System.out.println("received send: " + destination);
    }

    private final FrameOutput output;
}
