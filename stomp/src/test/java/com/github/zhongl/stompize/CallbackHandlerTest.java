package com.github.zhongl.stompize;

import io.netty.channel.Channel;
import org.junit.Test;

import static com.github.zhongl.stompize.Bytes.buf;
import static org.mockito.Mockito.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class CallbackHandlerTest {
    @Test
    public void shouldCallReceipt() throws Exception {
        StompV1_2 client = Stompize.create(StompV1_2.class, Client.class, mock(Channel.class), 1000L);
        StompV1_2 spy = spy(client);
        CallbackHandler handler = new CallbackHandler(4096, (Stompizeble) spy);

        handler.inboundBufferUpdated(null, buf("RECEIPT\nreceipt-id:1\n\n\u0000"));
        verify(spy).receipt(new StompV1_2.ReceiptId("1"));
    }
}
