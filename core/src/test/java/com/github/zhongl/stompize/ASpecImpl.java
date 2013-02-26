package com.github.zhongl.stompize;

import static com.github.zhongl.stompize.Bytes.bytes;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class ASpecImpl extends ASpec {
    public ASpecImpl(FrameVisitor visitor, boolean z, byte b, char c, short s, int i, float f, long l, double d) {
        super(visitor, z, b, c, s, i, f, l, d);
    }

    @Override
    @Client
    public void send(
            @Required("destination") String destination,
            @Optional("receipt") String receipt,
            Object content
    ) {
        FrameVisitor output = output();

        output.command(bytes("SEND"));

        output.required(bytes("destination"), destination);

        output.optional(bytes("receipt"), receipt);

        output.content(content);
    }
}
