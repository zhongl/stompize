package com.github.zhongl.stompize;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static com.github.zhongl.stompize.Bytes.buf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompClientTest {

    @Test
    public void shouldPingPong() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final String value = "stompize";

        SimpleServer.main(new String[]{"9992"});

        final Callback callback = new Callback() {
            @Override
            public void receive(Content content) {
                if (value.equals(content.value().toString(Bytes.UTF8)))
                    latch.countDown();
            }
        };

        StompClient client = Client.newInstance(StompClient.class,
                                                new InetSocketAddress(9992),
                                                4096,
                                                callback);


        final String destination = "dest";
        final String id = "sub";

        client.connect("1.2", "localhost", "nobody", "", null);
        client.subscribe(destination, id, null, null);
        client.send(destination, null, null, new Content(buf(value)));

        assertThat(latch.await(2L, TimeUnit.SECONDS), is(true));
    }


}
