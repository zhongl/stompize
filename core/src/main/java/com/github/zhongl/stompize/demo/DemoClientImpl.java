package com.github.zhongl.stompize.demo;

import com.github.zhongl.stompize.Content;
import com.github.zhongl.stompize.Optional;
import com.github.zhongl.stompize.Required;
import com.github.zhongl.stompize.Stompize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultCompositeByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;

import static com.github.zhongl.stompize.Bytes.buf;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class DemoClientImpl extends DemoClient {

    private static final ByteBuf SEND        = buf("SEND");
    private static final ByteBuf DESTINATION = buf("\ndestination:");
    private static final ByteBuf TRANSACTION = buf("\ntransaction:");
    private static final ByteBuf RECEIPT     = buf("\nreceipt:");
    private static final ByteBuf ACK         = buf("ACK");
    private static final ByteBuf ID          = buf("\nid:");

    private final Channel channel;

    public DemoClientImpl(Channel channel, Object o, Object o1) {
        super(o, o1);
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

        Stompize.addRequiredHeaderTo(components, DESTINATION, destination);
        Stompize.addOptionalHeaderTo(components, TRANSACTION, transaction);
        Stompize.addOptionalHeaderTo(components, RECEIPT, receipt);
        Stompize.addContentTo(components, content);

        channel.write(new DefaultCompositeByteBuf(UnpooledByteBufAllocator.HEAP_BY_DEFAULT, false, 32, components));
        channel.flush();
    }

    @Override
    public void ack(@Required("id") String id, @Optional("receipt") String receipt, @Optional("transaction") String transaction) {
        List<ByteBuf> components = new ArrayList<ByteBuf>();
        components.add(ACK);

        Stompize.addRequiredHeaderTo(components, ID, id);
        Stompize.addOptionalHeaderTo(components, RECEIPT, receipt);
        Stompize.addOptionalHeaderTo(components, TRANSACTION, transaction);
        Stompize.addContentTo(components, null);

        channel.write(new DefaultCompositeByteBuf(UnpooledByteBufAllocator.HEAP_BY_DEFAULT, false, 32, components));
        channel.flush();
    }


}
