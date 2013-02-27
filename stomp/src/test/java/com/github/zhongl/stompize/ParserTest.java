package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class ParserTest {
    @Test
    public void shouldNotParseAFrame() throws Exception {
        Frame frame = new Parser(4096).parse(Bytes.buf("SEND"));
        assertThat(frame, is(nullValue(Frame.class)));
    }

    @Test
    public void shouldComplainTooLongFrame() throws Exception {
        try {
            new Parser(11).parse(Bytes.buf("LONG_COMMAND\n"));
            fail();
        } catch (Parser.ParseException e) {
            assertThat(e.getMessage(), is("Frame should not longer than: 11."));
            assertThat(e.content(), CoreMatchers.is(Bytes.buf("LONG_COMMAND")));
        }
    }

    @Test
    public void shouldParseAContentFrame() throws Exception {
        ByteBuf buf = Bytes.buf("COMMAND\nname:value\n\ncontent\u0000");
        Frame frame = new Parser(4096).parse(buf);
        assertThat(frame.command(), CoreMatchers.is(Bytes.buf("COMMAND")));
        assertThat(frame.header(Bytes.buf("name")), is("value"));
        assertThat(frame.content(), is(new Content(Bytes.buf("content"))));
        assertThat(buf.isReadable(), is(false));
    }

    @Test
    public void shouldParseAFixedContentFrame() throws Exception {
        Frame frame = new Parser(4096).parse(Bytes.buf("COMMAND\nname:value\ncontent-length:7\n\ncontent\u0000"));
        assertThat(frame.command(), CoreMatchers.is(Bytes.buf("COMMAND")));
        assertThat(frame.header(Bytes.buf("name")), is("value"));
        assertThat(frame.content(), is(new Content(Bytes.buf("content"))));
    }

    @Test
    public void shouldParseAFixedJsonContentFrame() throws Exception {
        Frame frame = new Parser(4096).parse(Bytes.buf("COMMAND\nname:value\ncontent-type:text/json\ncontent-length:5\n\n[1,2]\u0000"));
        assertThat(frame.command(), CoreMatchers.is(Bytes.buf("COMMAND")));
        assertThat(frame.header(Bytes.buf("name")), is("value"));
        assertThat(frame.content(), is(new Content("text/json", Bytes.buf("[1,2]"))));
    }

    @Test
    public void shouldParseANonContentFrame() throws Exception {
        ByteBuf buf = Bytes.buf("COMMAND\nname:value\n\n\u0000");
        Frame frame = new Parser(4096).parse(buf);
        assertThat(frame.content(), CoreMatchers.is(Content.NONE));
        assertThat(buf.isReadable(), is(false));
    }

    @Test
    public void shouldParseAFrameUseCRLF() throws Exception {
        Frame frame = new Parser(4096).parse(Bytes.buf("COMMAND\r\nname:value\r\n\r\n\u0000"));
        assertThat(frame.command(), CoreMatchers.is(Bytes.buf("COMMAND")));
        assertThat(frame.header(Bytes.buf("name")), is("value"));
        assertThat(frame.content(), CoreMatchers.is(Content.NONE));
    }

    @Test
    public void shouldSkipEOLBeforeCommand() throws Exception {
        Frame frame = new Parser(4096).parse(Bytes.buf("\n\r\nCOMMAND\nname:value\n\n\u0000"));
        assertThat(frame.command(), CoreMatchers.is(Bytes.buf("COMMAND")));
        assertThat(frame.header(Bytes.buf("name")), is("value"));
        assertThat(frame.content(), CoreMatchers.is(Content.NONE));
    }

    @Test
    public void shouldComplainMissingCOLON() throws Exception {
        try {
            new Parser(4096).parse(Bytes.buf("COMMAND\nnamevalue\n\n\u0000"));
            fail();
        } catch (Parser.ParseException e) {
            assertThat(e.getMessage(), is("Missing COLON."));
            assertThat(e.content(), CoreMatchers.is(Bytes.buf("namevalue")));
        }
    }

    @Test
    public void shouldComplainEmptyHeaderName() throws Exception {
        try {
            new Parser(4096).parse(Bytes.buf("COMMAND\n:value\n\n\u0000"));
            fail();
        } catch (Parser.ParseException e) {
            assertThat(e.getMessage(), is("Empty header name."));
            assertThat(e.content(), CoreMatchers.is(Bytes.buf(":value")));
        }
    }

    @Test
    public void shouldComplainNonNullFrameEnd() throws Exception {
        try {
            new Parser(4096).parse(Bytes.buf("COMMAND\nname:value\ncontent-length:7\n\ncontent!\u0000"));
            fail();
        } catch (Parser.ParseException e) {
            assertThat(e.getMessage(), is("Non-NULL end of frame."));
            assertThat(e.content(), CoreMatchers.is(Bytes.buf("COMMAND\nname:value\ncontent-length:7\n\ncontent!")));
        }
    }

    @Test
    public void shouldParseFramesInBigBuf() throws Exception {
        ByteBuf in = Bytes.buf("C1\nh1:v1\n\n\u0000C2\nh2:v2\n\n\u0000C3\nh3:v3\n\nc3\u0000C4\nh4:v4\ncontent-length:7\n\ncontent\u0000");
        Parser parser = new Parser(4096);

        Frame f1 = parser.parse(in);
        assertThat(f1.command(), CoreMatchers.is(Bytes.buf("C1")));
        assertThat(f1.header(Bytes.buf("h1")), is("v1"));

        Frame f2 = parser.parse(in);
        assertThat(f2.command(), CoreMatchers.is(Bytes.buf("C2")));
        assertThat(f2.header(Bytes.buf("h2")), is("v2"));

        Frame f3 = parser.parse(in);
        assertThat(f3.content(), is(new Content(Bytes.buf("c3"))));

        Frame f4 = parser.parse(in);
        assertThat(f4.content(), is(new Content(Bytes.buf("content"))));

    }
}
