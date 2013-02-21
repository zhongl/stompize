package com.github.zhongl.stompize;

import com.github.zhongl.stompize.demo.Demo;
import com.github.zhongl.stompize.demo.DemoClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Ignore;
import org.junit.Test;

import static com.github.zhongl.stompize.Bytes.buf;
import static com.github.zhongl.stompize.Stompize.newInstance;
import static org.mockito.Mockito.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompizeTest {

    @Test
    public void shouldWriteSendFrameToChannel() throws Exception {
        Channel channel = mock(Channel.class);
        DemoClient client = newInstance(DemoClient.class, channel, "", "");

        client.send("d", new Content(buf("123")));
        verify(channel).write(buf("SEND\ndestination:d\ncontent-length:3\n\n123\u0000"));

        client.send("d", null, "r1", new Content("text/json", buf("[]")));
        verify(channel).write(buf("SEND\ndestination:d\nreceipt:r1\ncontent-type:text/json\ncontent-length:2\n\n[]\u0000"));

        client.ack("1");
        verify(channel).write(buf("ACK\nid:1\n\n\u0000"));
    }

    @Ignore
    public void shouldInvokeReceipt() throws Exception {
        Demo demo = mock(Demo.class);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        doReturn(buf("RECEIPT\nreceipt-id:1\n\n\u0000")).when(ctx).inboundByteBuffer();
        Stompize.inboundHandler(demo).inboundBufferUpdated(ctx);
        verify(demo).receipt("1");
    }

}
