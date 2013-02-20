package com.github.zhongl.stompize;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

import static com.github.zhongl.stompize.Bytes.buf;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompizeTest {

    private InetSocketAddress address;

    @Test
    public void shouldInvokeProxy() throws Exception {
        URI uri = new URI("stomp://app@localhost:9901");
        System.out.println(uri.getPort());
        System.out.println(uri.getHost());
        System.out.println(uri.getScheme());
        System.out.println(uri.getRawUserInfo());
//        Stompize.client(uri, DemoClient.class).newInstance();
    }

    @Test
    public void shouldPingPong() throws Exception {
        server();
        client().send("d", null, "1", new Content(buf("123")));
        Thread.sleep(1000);
    }

    private StompizedDemoClient client() {
        final Bootstrap bootstrap = new Bootstrap();
        try {
            final AtomicReference<StompizedDemoClient> ref = new AtomicReference<StompizedDemoClient>();
            bootstrap.group(new NioEventLoopGroup(1))
                     .channel(NioSocketChannel.class)
                     .remoteAddress(address)
                     .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         public void initChannel(SocketChannel ch) throws Exception {
                             StompizedDemoClient client = new StompizedDemoClient(ch);
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
        address = new InetSocketAddress(9901);
        bootstrap.group(group, group)
                 .channel(NioServerSocketChannel.class)
                 .localAddress(address)
                 .childHandler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     public void initChannel(SocketChannel ch) throws Exception {
                         ch.pipeline().addLast(new Handler(new StompizedDemoServer(ch)));
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
