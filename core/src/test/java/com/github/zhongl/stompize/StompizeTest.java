package com.github.zhongl.stompize;

import com.github.zhongl.stompize.demo.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

import static com.github.zhongl.stompize.Bytes.buf;
import static com.github.zhongl.stompize.Stompize.newInstance;
import static org.mockito.Mockito.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompizeTest {

    private InetSocketAddress address = new InetSocketAddress(9901);

    @Test
    public void shouldWriteSendFrameToChannel() throws Exception {
        Channel channel = mock(Channel.class);
        newInstance(DemoClient.class, channel, "", "").send("d", new Content(buf("123")));
        verify(channel).write(buf("SEND\ndestination:d\ncontent-length:3\n\n123\u0000"));
    }

    @Test
    public void shouldInvokeReceipt() throws Exception {
        Demo demo = mock(Demo.class);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        doReturn(buf("RECEIPT\nreceipt-id:1\n\n\u0000")).when(ctx).inboundByteBuffer();
        Stompize.inboundHandler(demo).inboundBufferUpdated(ctx);
        verify(demo).receipt("1");
    }

    @Test
    public void shouldPingPong() throws Exception {
        server();
        client().send("d", null, "1", new Content(buf("123")));
        Thread.sleep(1000);
    }

    private DemoClientImpl client() {
        final Bootstrap bootstrap = new Bootstrap();
        try {
            final AtomicReference<DemoClientImpl> ref = new AtomicReference<DemoClientImpl>();
            bootstrap.group(new NioEventLoopGroup(1))
                     .channel(NioSocketChannel.class)
                     .remoteAddress(address)
                     .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         public void initChannel(SocketChannel ch) throws Exception {
                             DemoClientImpl client = new DemoClientImpl(ch, null, null);
                             ref.set(client);
                             ch.pipeline().addLast(new Handler(client));

                             ch.closeFuture().addListener(new ChannelFutureListener() {
                                 @Override
                                 public void operationComplete(ChannelFuture future) throws Exception {
                                     bootstrap.shutdown();
                                 }
                             });

                         }
                     }).connect().sync().channel();

            return ref.get();
        } catch (InterruptedException e) {
            bootstrap.shutdown();
        }
        return null;
    }

    private void server() {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        final ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group, group)
                 .channel(NioServerSocketChannel.class)
                 .localAddress(address)
                 .childHandler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     public void initChannel(SocketChannel ch) throws Exception {
                         ch.pipeline().addLast(new Handler(new DemoServerImpl(ch)));
                         ch.closeFuture().addListener(new ChannelFutureListener() {
                             @Override
                             public void operationComplete(ChannelFuture future) throws Exception {
                                 bootstrap.shutdown();
                             }
                         });
                     }
                 });

        try {
            bootstrap.bind().sync();
        } catch (InterruptedException e) {
            bootstrap.shutdown();
        }
    }
}
