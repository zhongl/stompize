package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultCompositeByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.socket.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.github.zhongl.stompize.Bytes.buf;


/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompizedDemoServer extends DemoServer {

    private static final ByteBuf RECEIPTE    = buf("RECEIPT");
    private static final ByteBuf RECEIPTE_ID = buf("\nreceipt-id:");

    private final SocketChannel channel;

    public StompizedDemoServer(SocketChannel channel) {
        super();
        this.channel = channel;
    }

    @Override
    public void receipt(@Required("receipt-id") String receiptId) {

        List<ByteBuf> components = new ArrayList<ByteBuf>();
        components.add(RECEIPTE);

        if (receiptId == null) throw new IllegalArgumentException("Required header: receipt-id");
        components.add(RECEIPTE_ID);
        components.add(buf(receiptId));


        Content.NONE.appendTo(components);

        channel.write(new DefaultCompositeByteBuf(UnpooledByteBufAllocator.HEAP_BY_DEFAULT, false, 32, components));
        channel.flush();
    }

}
