package com.github.zhongl.stompize;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

import static com.github.zhongl.stompize.Bytes.buf;
import static com.github.zhongl.stompize.StompV1_2.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class SendingTest {

    @Test
    public void shouldOutputFrameWithNoContent() throws Exception {
        String command = "DISCONNECT";
        Receipt receipt = new Receipt("1");
        new Sending(channel, TIMEOUT, command, Arrays.<Header>asList(receipt), null) {
            @Override
            protected ChannelPromise promise() { return null; }
        }.run();

        verify(channel).write(buf(command));
        verify(channel).write(buf(receipt.toString()));
        verify(channel).write(buf("\n\n\u0000"));
        verify(channel).write(buf("\n\n"));
        verify(channel).flush();
    }

    @Test
    public void shouldOutputFrameWithContent() throws Exception {
        String command = "ERROR";
        Text content = new Text("content");
        new Sending(channel, TIMEOUT, command, Collections.<Header>emptySet(), content) {
            @Override
            protected ChannelPromise promise() { return null; }
        }.run();

        verify(channel).write(buf(command));
        verify(channel).write(buf(new ContentType(content.type()).toString()));
        verify(channel).write(buf(new ContentLength(content.length()).toString()));
        verify(channel).write(buf(content.toString()));
        verify(channel).write(buf("\n\n"));
        verify(channel).flush();
    }

    @Test
    public void shouldOutputPromiseSuccessDone() throws Exception {
        final ChannelPromise promise = mock(ChannelPromise.class);
        doReturn(true).when(promise).awaitUninterruptibly(TIMEOUT);
        doReturn(true).when(promise).isSuccess();

        String command = "ERROR";
        Text content = new Text("content");
        new Sending(channel, TIMEOUT, command, Collections.<Header>emptySet(), content) {
            @Override
            protected ChannelPromise promise() { return promise; }
        }.run();

        verify(promise).awaitUninterruptibly(TIMEOUT);
        verify(promise).isSuccess();
    }

    @Test
    public void shouldComplainWriteTimeout() throws Exception {
        doReturn(false).when(future).awaitUninterruptibly(TIMEOUT);
        String command = "ERROR";
        Sending sending;
        sending = new Sending(channel, TIMEOUT, command, Collections.<Header>emptySet(), null) {
            @Override
            protected ChannelPromise promise() { return null; }
        };

        try {
            sending.run();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("Channel write timeout."));
        }

        final ChannelPromise promise = mock(ChannelPromise.class);
        doReturn(false).when(promise).awaitUninterruptibly(TIMEOUT);
        doReturn(false).when(future).isSuccess();

        sending = new Sending(channel, TIMEOUT, command, Collections.<Header>emptySet(), null) {
            @Override
            protected ChannelPromise promise() { return promise; }
        };

        try {
            sending.run();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("Channel write timeout."));
        }
    }

    @Test
    public void shouldComplainWriteFailure() throws Exception {
        doReturn(true).when(future).awaitUninterruptibly(TIMEOUT);
        doReturn(false).when(future).isSuccess();
        doReturn(new StompizeException("test")).when(future).cause();
        String command = "ERROR";
        Sending sending = new Sending(channel, TIMEOUT, command, Collections.<Header>emptySet(), null) {
            @Override
            protected ChannelPromise promise() { return null; }
        };

        try {
            sending.run();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("test"));
        }
    }

    @Test
    public void shouldComplainResponseTimeout() throws Exception {
        String command = "ERROR";
        Sending sending;

        final ChannelPromise promise = mock(ChannelPromise.class);
        doReturn(false).when(promise).awaitUninterruptibly(TIMEOUT);
        doReturn(true).when(future).isSuccess();

        sending = new Sending(channel, TIMEOUT, command, Collections.<Header>emptySet(), null) {
            @Override
            protected ChannelPromise promise() { return promise; }
        };

        try {
            sending.run();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("Server response timeout."));
        }
    }

    @Test
    public void shouldComplainResponseFailure() throws Exception {
        String command = "ERROR";
        Sending sending;

        final ChannelPromise promise = mock(ChannelPromise.class);
        doReturn(true).when(promise).awaitUninterruptibly(TIMEOUT);
        doReturn(false).when(promise).isSuccess();
        doReturn(new StompizeException("test")).when(promise).cause();

        sending = new Sending(channel, TIMEOUT, command, Collections.<Header>emptySet(), null) {
            @Override
            protected ChannelPromise promise() { return promise; }
        };

        try {
            sending.run();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("test"));
        }
    }

    @Before
    public void setUp() throws Exception {
        doReturn(true).when(future).awaitUninterruptibly(TIMEOUT);
        doReturn(true).when(future).isSuccess();
        doReturn(future).when(channel).flush();
    }

    static final long TIMEOUT = 1000L;

    final Channel       channel = mock(Channel.class);
    final ChannelFuture future  = mock(ChannelFuture.class);

}
