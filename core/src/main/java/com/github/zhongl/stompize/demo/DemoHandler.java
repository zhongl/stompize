package com.github.zhongl.stompize.demo;

import com.github.zhongl.stompize.Frame;
import com.github.zhongl.stompize.InboundHandler;
import io.netty.buffer.ByteBuf;

import static com.github.zhongl.stompize.Bytes.buf;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class DemoHandler extends InboundHandler {

    protected DemoHandler(DemoClient demo, int maxFrameLength) {
        super(maxFrameLength);
        this.demo = demo;
    }

    @Override
    protected void receive(Frame frame) {
        if (frame.command().equals(SEND)) {
            demo.send(frame.header(DESTINATION),
                      frame.header(TRANSACTION),
                      frame.header(RECEIPT),
                      frame.content());
            return;
        }

        if (frame.command().equals(SEND)) {
            demo.send(frame.header(DESTINATION),
                      frame.header(TRANSACTION),
                      frame.header(RECEIPT),
                      frame.content());
            return;
        }

    }

    public static final ByteBuf SEND        = buf("SEND");
    public static final ByteBuf DESTINATION = buf("destination");
    public static final ByteBuf TRANSACTION = buf("transaction");
    public static final ByteBuf RECEIPT     = buf("receipt");

    private final DemoClient demo;
}
