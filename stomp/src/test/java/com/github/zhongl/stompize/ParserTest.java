package com.github.zhongl.stompize;

import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class ParserTest {
    @Test
    public void shouldNotParseAFrame() throws Exception {
        new Parser(4096, stompizeble).parse(Bytes.buf("SEND"));
        verify(stompizeble, never()).apply(anyString(), any(Map.class), any());
    }

    @Test
    public void shouldComplainTooLongFrame() throws Exception {
        try {
            new Parser(11, stompizeble).parse(Bytes.buf("LONG_COMMAND\n"));
            fail();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("Frame should not longer than: 11.\nLONG_COMMAND"));
        }
    }

    @Test
    public void shouldParseAContentFrame() throws Exception {
        ByteBuf buf = Bytes.buf("COMMAND\nname:value\n\ncontent\u0000");
        new Parser(4096, stompizeble).parse(buf);
        verify(stompizeble).apply("COMMAND", Collections.singletonMap("name", "value"), Bytes.buf("content"));
        assertThat(buf.isReadable(), is(false));
    }

    @Test
    public void shouldParseAFixedContentFrame() throws Exception {
        new Parser(4096, stompizeble).parse(Bytes.buf("COMMAND\nname:value\ncontent-length:7\n\ncontent\u0000"));
        Map<String, String> headers = Splitter.on(',').withKeyValueSeparator(':').split("name:value,content-length:7");
        verify(stompizeble).apply("COMMAND", headers, Bytes.buf("content"));
    }

    @Test
    public void shouldParseAFixedJsonContentFrame() throws Exception {
        new Parser(4096, stompizeble).parse(Bytes.buf("COMMAND\nname:value\ncontent-type:text/json\ncontent-length:5\n\n[1,2]\u0000"));
        Map<String, String> headers = Splitter.on(',').withKeyValueSeparator(':').split("name:value,content-length:5,content-type:text/json");
        verify(stompizeble).apply("COMMAND", headers, Bytes.buf("[1,2]"));
    }

    @Test
    public void shouldParseANonContentFrame() throws Exception {
        ByteBuf buf = Bytes.buf("COMMAND\nname:value\n\n\u0000");
        new Parser(4096, stompizeble).parse(buf);
        verify(stompizeble).apply("COMMAND", Collections.singletonMap("name", "value"), Unpooled.EMPTY_BUFFER);
        assertThat(buf.isReadable(), is(false));
    }

    @Test
    public void shouldParseAFrameUseCRLF() throws Exception {
        new Parser(4096, stompizeble).parse(Bytes.buf("COMMAND\r\nname:value\r\n\r\n\u0000"));
        verify(stompizeble).apply("COMMAND", Collections.singletonMap("name", "value"), Unpooled.EMPTY_BUFFER);
    }

    @Test
    public void shouldSkipEOLBeforeCommand() throws Exception {
        new Parser(4096, stompizeble).parse(Bytes.buf("\n\r\nCOMMAND\nname:value\n\n\u0000"));
        verify(stompizeble).apply("COMMAND", Collections.singletonMap("name", "value"), Unpooled.EMPTY_BUFFER);
    }

    @Test
    public void shouldComplainMissingCOLON() throws Exception {
        try {
            new Parser(4096, stompizeble).parse(Bytes.buf("COMMAND\nnamevalue\n\n\u0000"));
            fail();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("Missing COLON.\nnamevalue"));
        }
    }

    @Test
    public void shouldComplainEmptyHeaderName() throws Exception {
        try {
            new Parser(4096, stompizeble).parse(Bytes.buf("COMMAND\n:value\n\n\u0000"));
            fail();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("Empty header name.\n:value"));
        }
    }

    @Test
    public void shouldComplainNonNullFrameEnd() throws Exception {
        try {
            new Parser(4096, stompizeble).parse(Bytes.buf("COMMAND\nname:value\ncontent-length:7\n\ncontent!\u0000"));
            fail();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("Non-NULL end of frame.\nCOMMAND\nname:value\ncontent-length:7\n\ncontent!"));
        }
    }

    @Test
    public void shouldParseFramesInBigBuf() throws Exception {
        ByteBuf in = Bytes.buf("C1\nh1:v1\n\n\u0000C2\nh2:v2\n\n\u0000C3\nh3:v3\n\nc3\u0000C4\nh4:v4\ncontent-length:7\n\ncontent\u0000");
        Parser parser = new Parser(4096, stompizeble);

        parser.parse(in);
        verify(stompizeble).apply("C1", Collections.singletonMap("h1", "v1"), Unpooled.EMPTY_BUFFER);

        parser.parse(in);
        verify(stompizeble).apply("C2", Collections.singletonMap("h2", "v2"), Unpooled.EMPTY_BUFFER);

        parser.parse(in);
        verify(stompizeble).apply("C3", Collections.singletonMap("h3", "v3"), Bytes.buf("c3"));

        parser.parse(in);
        Map<String, String> headers = Splitter.on(',').withKeyValueSeparator(':').split("h4:v4,content-length:7");
        verify(stompizeble).apply("C4", headers, Bytes.buf("content"));

    }

    Stompizeble stompizeble = mock(Stompizeble.class);
}