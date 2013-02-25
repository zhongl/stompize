package com.github.zhongl.stompize;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static com.github.zhongl.stompize.Bytes.buf;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompClientTest {

    public static final String[] ARGS = new String[0];

    @Test
    public void should() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        SimpleServer.main(ARGS);

        StompClient client = new StompClient("localhost", 9991).connect();

        String destination = "destination";

        client.subscribe(destination, new StompClient.Callback() {
            @Override
            public void receive(Content content) {
                latch.countDown();
            }
        });

        client.send(destination, new Content(buf("content")));
        latch.await(2L, TimeUnit.SECONDS);
    }
}
