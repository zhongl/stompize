package com.github.zhongl.stompize;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.github.zhongl.stompize.StompV1_2.*;
import static com.github.zhongl.stompize.StompV1_2.Ack.Type.Auto;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class ClientTest {

    public ClientTest() throws Exception {
        connected = mock(ChannelPromise.class);
        future = mock(ChannelFuture.class);
        promise = mock(ChannelPromise.class);
        channel = mock(Channel.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (!first) return promise;
                first = false;
                return connected;
            }
        }).when(channel).newPromise();

        doReturn(future).when(channel).flush();

        client = Stompize.create(StompV1_2.class, Client.class, channel, TIMEOUT);
    }

    @Test
    public void shouldComplainServerHasNotConnected() throws Exception {
        doReturn(false).when(connected).isDone();

        try {
            client.send(new Destination("/a/b"), new Text("content"), new Transaction("tx01"));
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Non-connecting frame allow before Server would be connected."));
        }
    }

    @Test
    public void shouldComplainServerAlreayConnected() throws Exception {
        doReturn(true).when(connected).isDone();
        doReturn(true).when(connected).isSuccess();

        try {
            client.connect(new AcceptVersion("1.2"), new Host("localhost"), new Login("root"), new Passcode("secret"), new Heartbeat(1, 2));
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Unnecessary connecting frame after Server has connected."));
        }
    }

    @Test
    public void shouldComplainServerConnectFailed() throws Exception {
        doReturn(true).when(connected).isDone();
        doReturn(false).when(connected).isSuccess();

        try {
            client.subscribe(new Destination("/a/b"), new Id("sub-001"), new Ack(Auto));
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Failed to connect server."));
        }
    }

    @Test
    public void shouldSetPromiseSuccess() throws Exception {
        doReturn(true).when(connected).isDone();
        doReturn(true).when(connected).isSuccess();

        doReturn(true).when(promise).awaitUninterruptibly(TIMEOUT);
        doReturn(true).when(promise).isSuccess();

        client.send(new Destination("/a/b"), new Text("content"), new Receipt("1"));
        client.receipt(new ReceiptId("1"));

        verify(promise).setSuccess();
    }

    @Test
    public void shouldSetPromiseFailure() throws Exception {
        doReturn(true).when(connected).isDone();
        doReturn(true).when(connected).isSuccess();

        doReturn(true).when(promise).awaitUninterruptibly(TIMEOUT);
        doReturn(false).when(promise).isSuccess();
        doReturn(new StompizeException("test")).when(promise).cause();

        try {
            client.send(new Destination("/a/b"), new Text("content"), new Receipt("1"));
        } catch (StompizeException e) { }
        client.error(new Text("test"), new ReceiptId("1"));

        verify(promise).setFailure(any(Throwable.class));
    }

    @Test
    public void shouldSetConnectedSuccess() throws Exception {
        doReturn(false).when(connected).isDone();

        doReturn(true).when(connected).awaitUninterruptibly(TIMEOUT);
        doReturn(true).when(connected).isSuccess();

        client.stomp(new AcceptVersion("1.2"), new Host("localhost"));
        client.connected(new Version("1.2"), new Server("stompize-stomp", "1.2.0.1"), new Session("91883719"));

        verify(connected).setSuccess();
    }

    @Test
    public void shouldSetConnectedFailure() throws Exception {
        doReturn(false).when(connected).isDone();

        doReturn(true).when(connected).awaitUninterruptibly(TIMEOUT);
        doReturn(false).when(connected).isSuccess();
        doReturn(new StompizeException("test")).when(connected).cause();

        try {
            client.stomp(new AcceptVersion("1.2"), new Host("localhost"));
        } catch (StompizeException e) { }
        client.error(new Text("test"), new Message("StompizeException"));

        verify(connected).setFailure(any(Throwable.class));
    }

    @Test
    public void shouldComplainDuplicationReceipt() throws Exception {
        doReturn(true).when(connected).isDone();
        doReturn(true).when(connected).isSuccess();

        doReturn(true).when(promise).awaitUninterruptibly(TIMEOUT);
        doReturn(true).when(promise).isSuccess();

        try {
            client.send(new Destination("/a/b"), new Text("content1"), new Receipt("1"));
            client.send(new Destination("/a/b"), new Text("content2"), new Receipt("1"));
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Duplicated receipt: 1."));
        }
    }

    static final long TIMEOUT = 1000L;

    final Channel        channel;
    final ChannelPromise connected;
    final ChannelFuture  future;
    final ChannelPromise promise;
    final StompV1_2      client;

    boolean first = true;
}
