package com.github.zhongl.stompize;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.CountDownLatch;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompClient {

    public StompClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public StompClient connect() throws InterruptedException {
        bootstrap.group(new NioEventLoopGroup(1))
                 .channel(NioSocketChannel.class)
                 .remoteAddress(host, port)
                 .handler(new ChannelInitializer<Channel>() {
                     @Override
                     protected void initChannel(Channel ch) throws Exception {
                         handle = Stompize.newInstance(Handle.class, ch, StompClient.this);
                         ChannelHandler inboundHandler = Stompize.inboundHandler(handle, 4096);
                         ChannelHandler errorHandler = new ChannelInboundByteHandlerAdapter() {

                             @Override
                             protected void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
                                 ctx.fireInboundBufferUpdated();
                             }

                             @Override
                             public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                 handle.error(cause.getMessage(), Content.NONE);
                             }
                         };
                         ch.pipeline().addLast(inboundHandler);
                     }
                 })
                 .connect(host, port).sync();
        handle.connect("1.2", "", null, null, null);
        latch.await();
        return this;
    }

    public void disconnect() {
        if (handle != null) handle.disconnect(null);
        bootstrap.shutdown();
    }

    public void subscribe(String destination, Callback callback) {
        this.callback = callback;
        handle.subscribe(destination, session, null, null);
    }

    public void send(String destination, Content content) {
        handle.send(destination, null, null, content);
    }

    abstract static class Handle implements StompV1_2 {

        private final StompClient client;

        protected Handle(StompClient client) {this.client = client;}

        @Override
        public void connected(@Required("version") String version, @Optional("session") String session, @Optional("server") String server, @Optional("heart-beat") String heartBeat) {
            client.session = session;
            client.latch.countDown();
        }

        @Override
        public void receipt(@Required("receipt-id") String receiptId) {
            System.out.println("recept-id: " + receiptId);
        }

        @Override
        public void error(@Optional("message") String message, Content content) {
            System.err.println(message);
        }

        @Override
        public void message(@Required("destination") String destination, @Required("message-id") String messageId, @Required("subscription") String subscription, @Optional("ack") String ack, Content content) {
            System.out.println("Client received message");
            if (client.callback != null) client.callback.receive(content);
            ack(ack == null ? messageId : ack, null, null);
        }

    }

    public interface Callback {
        void receive(Content content);
    }

    private final Bootstrap bootstrap = new Bootstrap();
    private final String host;
    private final int    port;
    private final CountDownLatch latch = new CountDownLatch(1);

    private volatile Handle   handle;
    private volatile String   session;
    private volatile Callback callback;
}
