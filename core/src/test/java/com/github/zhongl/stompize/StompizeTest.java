package com.github.zhongl.stompize;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompizeTest {
    @Test
    public void shouldStompizeASpec() throws Exception {
        FrameVisitor visitor = mock(FrameVisitor.class);
        Object[] arguments = {visitor, false, Byte.MIN_VALUE, 'a', Short.MIN_VALUE, 1, 2.0f, 1L, 2.0};
        ASpec aSpec = Stompize.newInstance(ASpec.class, arguments);

        aSpec.send("d", null, "c");

        verify(visitor).command("SEND");
        verify(visitor).header("destination", "d", true);
        verify(visitor).header("receipt", null, false);
        verify(visitor).content("c");
    }

}
