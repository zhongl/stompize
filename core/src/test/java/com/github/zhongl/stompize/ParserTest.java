package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static com.github.zhongl.stompize.Bytes.buf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class ParserTest {
    @Test
    public void shouldNotParseAFrame() throws Exception {
        Frame frame = new Parser(4096).parse(buf("SEND"));
        assertThat(frame, is(nullValue(Frame.class)));
    }

    @Test
    public void shouldComplainTooLongFrame() throws Exception {
        try {
            new Parser(11).parse(buf("LONG_COMMAND\n"));
            fail();
        } catch (Parser.ParseException e) {
            assertThat(e.getMessage(), is("Frame should not longer than: 11."));
            assertThat(e.content(), is("LONG_COMMAND"));
        }
    }

    @Test
    public void shouldParseAContentFrame() throws Exception {
        ByteBuf buf = buf("COMMAND\nname:value\n\ncontent\u0000");
        Frame frame = new Parser(4096).parse(buf);
        assertThat(frame.command(), is(buf("COMMAND")));
        assertThat(frame.header(buf("name")), is("value"));
        assertThat(frame.content(), is(new Content(buf("content"))));
        assertThat(buf.isReadable(), is(false));
    }

    @Test
    public void shouldParseAFixedContentFrame() throws Exception {
        Frame frame = new Parser(4096).parse(buf("COMMAND\nname:value\ncontent-length:7\n\ncontent\u0000"));
        assertThat(frame.command(), is(buf("COMMAND")));
        assertThat(frame.header(buf("name")), is("value"));
        assertThat(frame.content(), is(new Content(buf("content"))));
    }

    @Test
    public void shouldParseAFixedJsonContentFrame() throws Exception {
        Frame frame = new Parser(4096).parse(buf("COMMAND\nname:value\ncontent-type:text/json\ncontent-length:5\n\n[1,2]\u0000"));
        assertThat(frame.command(), is(buf("COMMAND")));
        assertThat(frame.header(buf("name")), is("value"));
        assertThat(frame.content(), is(new Content("text/json", buf("[1,2]"))));
    }

    @Test
    public void shouldParseANonContentFrame() throws Exception {
        ByteBuf buf = buf("COMMAND\nname:value\n\n\u0000");
        Frame frame = new Parser(4096).parse(buf);
        assertThat(frame.content(), is(Content.NONE));
        assertThat(buf.isReadable(), is(false));
    }

    @Test
    public void shouldParseAFrameUseCRLF() throws Exception {
        Frame frame = new Parser(4096).parse(buf("COMMAND\r\nname:value\r\n\r\n\u0000"));
        assertThat(frame.command(), is(buf("COMMAND")));
        assertThat(frame.header(buf("name")), is("value"));
        assertThat(frame.content(), is(Content.NONE));
    }

    @Test
    public void shouldSkipEOLBeforeCommand() throws Exception {
        Frame frame = new Parser(4096).parse(buf("\n\r\nCOMMAND\nname:value\n\n\u0000"));
        assertThat(frame.command(), is(buf("COMMAND")));
        assertThat(frame.header(buf("name")), is("value"));
        assertThat(frame.content(), is(Content.NONE));
    }

    @Test
    public void shouldComplainMissingCOLON() throws Exception {
        try {
            new Parser(4096).parse(buf("COMMAND\nnamevalue\n\n\u0000"));
            fail();
        } catch (Parser.ParseException e) {
            assertThat(e.getMessage(), is("Missing COLON."));
            assertThat(e.content(), is("namevalue"));
        }
    }

    @Test
    public void shouldComplainEmptyHeaderName() throws Exception {
        try {
            new Parser(4096).parse(buf("COMMAND\n:value\n\n\u0000"));
            fail();
        } catch (Parser.ParseException e) {
            assertThat(e.getMessage(), is("Empty header name."));
            assertThat(e.content(), is(":value"));
        }
    }

    @Test
    public void shouldComplainNonNullFrameEnd() throws Exception {
        try {
            new Parser(4096).parse(buf("COMMAND\nname:value\ncontent-length:7\n\ncontent!\u0000"));
            fail();
        } catch (Parser.ParseException e) {
            assertThat(e.getMessage(), is("Non-NULL end of frame."));
            assertThat(e.content(), is("COMMAND\nname:value\ncontent-length:7\n\ncontent!"));
        }
    }

}
