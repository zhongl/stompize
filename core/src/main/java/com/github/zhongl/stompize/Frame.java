package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;

import static com.github.zhongl.stompize.Bytes.NULL;
import static com.github.zhongl.stompize.Bytes.UTF8;
import static com.github.zhongl.stompize.Bytes.buf;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class Frame {

    private final String[] lines;

    public static Frame decode(ByteBuf in) {
        int length = in.bytesBefore(NULL);
        if (length == -1) return null;
        String[] lines = in.readSlice(length).toString(UTF8).split("\n");
        in.skipBytes(1);
        return new Frame(lines);
    }

    private Frame(String[] lines) {
        this.lines = lines;
    }


    public String command() {
        return lines[0];  // TODO
    }

    public String header(String name) {
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith(name)) return line.substring(name.length() + 1);
        }
        return null;
    }

    public Content content() {
        return new Content(buf(lines[lines.length - 1]));
    }
}
