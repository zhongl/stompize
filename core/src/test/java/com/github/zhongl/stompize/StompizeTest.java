package com.github.zhongl.stompize;

import org.junit.Test;

import static org.mockito.Mockito.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompizeTest {
    @Test
    public void shouldOutputFrameWithContent() throws Exception {
        FrameOutput output = mock(FrameOutput.class);
        Object[] arguments = {output, false, Byte.MIN_VALUE, 'a', Short.MIN_VALUE, 1, 2.0f, 1L, 2.0};
        StompClient stompClient = Stompize.newInstance(StompClient.class, arguments);

        stompClient.send("d", null, "c");

        verify(output).command("SEND");
        verify(output).header("destination", "d", true);
        verify(output).header("receipt", null, false);
        verify(output).content("c");
    }

    @Test
    public void shouldOutputFrameWithoutContent() throws Exception {
        FrameOutput output = mock(FrameOutput.class);
        StompServer stompServer = Stompize.newInstance(StompServer.class, output);

        stompServer.receipt("1");

        verify(output).command("RECEIPT");
        verify(output).header("receipt-id", "1", false);
    }

    @Test
    public void shouldCallbackFrameWithoutContent() throws Exception {
        StompClient stompClient = mock(StompClient.class);
        Frame frame = mock(Frame.class);

        doReturn(true).when(frame).isCommand("RECEIPT");
        doReturn("1").when(frame).header("receipt-id");

        Stompize.callback(stompClient).apply(frame);

        verify(stompClient).receipt("1");
    }

    @Test
    public void shouldCallbackFrameWithContent() throws Exception {
        StompServer stompClient = mock(StompServer.class);
        Frame frame = mock(Frame.class);

        doReturn(true).when(frame).isCommand("SEND");
        doReturn("d").when(frame).header("destination");
        doReturn("c").when(frame).content();

        Stompize.callback(stompClient).apply(frame);

        verify(stompClient).send("d", null, "c");
    }
}
