package com.github.zhongl.stompize;

import com.google.common.base.Function;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static com.github.zhongl.stompize.Stomp.Destination;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompizeTest {
    @Test
    public void shouldOutputFrameWithContent() throws Exception {
        Function<String, Void> output = mock(Function.class);
        Stomp client = Stompize.create(Stomp.class, StompClient.class, output);

        Destination d = new Destination("d");
        client.send(d, new Stomp.Text("content"));

        verify(output).apply("[SEND, [[\ndestination:d], \n\ncontent\u0000]]");
    }

    @Test
    public void shouldCallbackCommand() throws Exception {
        Function<String, Void> output = mock(Function.class);
        Stompizeble stompizeble = (Stompizeble) Stompize.create(Stomp.class, StompClient.class, output);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("message", "IMOP");
        stompizeble.apply("ERROR", headers, "OMG");

        verify(output).apply("[ERROR, [\n\nOMG\u0000, [\nmessage:IMOP]]]");
    }

}
