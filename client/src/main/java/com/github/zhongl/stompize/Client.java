package com.github.zhongl.stompize;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public final class Client {

    public static <T extends StompV1_2> T newInstance(
            Class<T> spec,
            InetSocketAddress address,
            int maxFrameLength,
            Object... arguments
    ) throws Exception {
        Initializer<T> initializer = new Initializer<T>(spec, maxFrameLength, arguments);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1))
                 .channel(NioSocketChannel.class)
                 .remoteAddress(address)
                 .handler(initializer)
                 .connect().sync();
        return initializer.initialized();
    }

    private Client() {}
}
