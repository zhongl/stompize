package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.Map;

import static com.github.zhongl.stompize.Bytes.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
class Parser {

    Parser(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
        last = start();
    }

    public Frame parse(ByteBuf in) {
        for (; ; ) {
            State<?> s = last.read(in);

            if (s instanceof Broken) {
                Broken b = (Broken) s;
                last = b.get();
                return null;
            }

            if (s instanceof Done) {
                Done d = (Done) s;
                last = start();
                return d.get();
            }

            if (s instanceof Next) {
                Next n = (Next) s;
                last = n.get();
            }
        }
    }

    private Activity start() { return new ReadCommand(0); }

    private final int maxFrameLength;

    private Activity last;


    private class ReadCommand extends ReadLine {

        ReadCommand(int offset) { super(offset); }

        @Override
        protected State<?> state(ByteBuf command, int offset) {
            return new Next(command.isReadable() ?
                                    new ReadHeader(offset, command, new Headers()) :
                                    new ReadCommand(offset));
        }
    }

    private class ReadHeader extends ReadLine {

        ReadHeader(int offset, ByteBuf command, Headers headers) {
            super(offset);
            this.command = command;
            this.headers = headers;
        }

        @Override
        protected State<?> state(ByteBuf header, int offset) {
            return new Next(header.isReadable() ?
                                    new ReadHeader(offset, command, headers.append(header)) :
                                    new ReadContent(offset, command, headers));
        }

        final ByteBuf command;
        final Headers headers;
    }

    private static class Headers {
        final Map<ByteBuf, ByteBuf> map = new HashMap<ByteBuf, ByteBuf>();

        public Headers append(ByteBuf header) {
            int l = header.bytesBefore(COLON);
            if (l == -1) throw new ParseException("Missing COLON.", header);

            ByteBuf name = header.readSlice(l);
            if (!name.isReadable()) throw new ParseException("Empty header name.", header);

            header.skipBytes(1); // skip COLON

            if (!map.containsKey(name)) map.put(name, header);

            return this;
        }

        public int contentLength() {
            ByteBuf value = map.get(buf("content-length"));
            if (value == null) return -1;
            return Integer.valueOf(value.toString(UTF8));
        }

        public String contentType() {
            ByteBuf value = map.get(buf("content-type"));
            return value == null ? null : value.toString(UTF8);
        }
    }

    private class ReadContent implements Activity {

        public ReadContent(int offset, ByteBuf command, Headers headers) {
            this.offset = offset;
            this.command = command;
            this.headers = headers;
        }

        @Override
        public State<?> read(ByteBuf in) {
            int l = headers.contentLength();
            ByteBuf content = l == -1 ? readContent(in) : readFixedContent(l, in);
            return new Done(new Frame(command, headers.map, new Content(headers.contentType(), content)));
        }

        private ByteBuf readContent(ByteBuf in) {
            State<?> ref = new ReadUtil(NULL, offset) {
                @Override
                protected State<?> next(ByteBuf in, int length) {
                    ByteBuf content = in.slice(in.readerIndex() + super.offset, length);
                    in.skipBytes(super.offset + length + 1); // skip NULL
                    return new State<Object>(content) {};
                }
            }.read(in);
            return (ByteBuf) ref.get();
        }

        private ByteBuf readFixedContent(int length, ByteBuf in) {
            int start = in.readerIndex() + offset;
            ByteBuf content = in.slice(start, length);
            if (in.getByte(start + length) != NULL)
                throw new ParseException("Non-NULL end of frame.", in.readSlice(offset + length + 1));

            in.skipBytes(offset + length + 1);
            return content;
        }

        final int     offset;
        final ByteBuf command;
        final Headers headers;
    }

    private abstract class ReadUtil implements Activity {
        protected ReadUtil(byte b, int offset) {
            this.b = b;
            this.offset = offset;
        }

        @Override
        public final State<?> read(ByteBuf in) {
            int start = in.readerIndex() + offset;
            int max = in.writerIndex();

            for (int i = start; ; i++) {
                if (i > maxFrameLength) throw new ParseException(tooLongFrame(), in.readSlice(i));
                if (i == max) return new Broken(this);
                if (in.getByte(i) == b) return next(in, i - start);
            }
        }

        protected abstract State<?> next(ByteBuf in, int length);

        private String tooLongFrame() {return "Frame should not longer than: " + maxFrameLength + '.';}

        final byte b;
        final int  offset;
    }

    private abstract class ReadLine extends ReadUtil {
        ReadLine(int offset) { super(LF, offset); }

        @Override
        protected State<?> next(ByteBuf in, int length) {
            int start = in.readerIndex() + offset;
            int readerIndex = offset + length + 1;
            if (length == 0) return state(Unpooled.EMPTY_BUFFER, readerIndex);
            if (in.getByte(start + length - 1) == CR) length -= 1;
            return state(in.slice(start, length), readerIndex);
        }

        protected abstract State<?> state(ByteBuf buf, int offset);
    }

    private static abstract class State<T> {
        protected State(T value) {this.value = value;}

        T get() {return value;}

        final T value;
    }

    private static class Broken extends State<Activity> {
        protected Broken(Activity activity) { super(activity); }
    }

    private static class Next extends State<Activity> {
        protected Next(Activity activity) { super(activity); }
    }

    private static class Done extends State<Frame> {
        protected Done(Frame frame) { super(frame); }
    }

    private interface Activity {
        State<?> read(ByteBuf in);
    }

    static class ParseException extends RuntimeException {

        ParseException(String message, ByteBuf buf) {
            super(message);
            this.buf = buf;
        }

        String content() { return buf.toString(UTF8); }

        final ByteBuf buf;

        private static final long serialVersionUID = 9000540009058302926L;
    }

}
