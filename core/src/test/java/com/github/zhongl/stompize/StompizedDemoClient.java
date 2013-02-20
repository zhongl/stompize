package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultCompositeByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.socket.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.github.zhongl.stompize.Bytes.buf;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompizedDemoClient extends DemoClient {

    private static final ByteBuf SEND        = buf("SEND");
    private static final ByteBuf DESTINATION = buf("\ndestination:");
    private static final ByteBuf TRANSACTION = buf("\ntransaction:");
    private static final ByteBuf RECEIPT     = buf("\nreceipt:");

    private final SocketChannel channel;

    public StompizedDemoClient(SocketChannel channel) {
        super();
        this.channel = channel;
    }

    @Override
    public void send(
            @Required("destination") String destination,
            @Optional("transaction") String transaction,
            @Optional("receipt") String receipt,
            Content content
    ) {
        List<ByteBuf> components = new ArrayList<ByteBuf>();
        components.add(SEND);

        if (destination == null) throw new IllegalArgumentException("Required header: destination");
        components.add(DESTINATION);
        components.add(buf(destination));

        if (transaction != null) {
            components.add(TRANSACTION);
            components.add(buf(transaction));
        }

        if (receipt != null) {
            components.add(RECEIPT);
            components.add(buf(receipt));
        }

        content.appendTo(components);

        channel.write(new DefaultCompositeByteBuf(UnpooledByteBufAllocator.HEAP_BY_DEFAULT, false, 32, components));
        channel.flush();
    }


}
