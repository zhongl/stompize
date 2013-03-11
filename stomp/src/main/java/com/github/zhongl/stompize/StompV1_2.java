package com.github.zhongl.stompize;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;

import static com.github.zhongl.stompize.Bytes.UTF8;
import static com.github.zhongl.stompize.Bytes.buf;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public interface StompV1_2 extends Specification {

    @Command(optionals = {Login.class, Passcode.class, Heartbeat.class})
    void connect(AcceptVersion acceptVersion, Host host, Header... optionals);

    @Command(optionals = {Login.class, Passcode.class, Heartbeat.class})
    void stomp(AcceptVersion acceptVersion, Host host, Header... optionals);

    @Command(optionals = {Transaction.class, Receipt.class})
    void connected(Version version, Header... optionals);

    @Command(optionals = {Transaction.class, Receipt.class})
    void send(Destination destination, Text content, Header... optionals);

    @Command(optionals = {Receipt.class, Ack.class})
    void subscribe(Destination destination, Id id, Header... optionals);

    @Command(optionals = Receipt.class)
    void unsubscribe(Id id, Header... optionals);

    @Command(optionals = {Receipt.class, Transaction.class})
    void ack(Id id, Header... optionals);

    @Command(optionals = {Receipt.class, Transaction.class})
    void nack(Id id, Header... optionals);

    @Command(optionals = Receipt.class)
    void begin(Transaction transaction, Header... optionals);

    @Command(optionals = Receipt.class)
    void commit(Transaction transaction, Header... optionals);

    @Command(optionals = Receipt.class)
    void abort(Transaction transaction, Header... optionals);

    @Command(optionals = Receipt.class)
    void disconnect(Header... optionals);

    @Command
    void receipt(ReceiptId receiptId);

    @Command(optionals = Ack.class)
    void message(Destination destination, MessageId messageId, Subscription subscription, Text content, Header... optionals);

    @Command(optionals = {Message.class, ReceiptId.class})
    void error(Text content, Header... optionals);

    class Text extends Content<ByteBuf> {

        public Text(String value) { this(buf(value), null); }

        public Text(Object value, String type) { super((ByteBuf) value, type == null ? "text/plain" : type); }

        @Override
        protected String value() { return value.toString(UTF8); }
    }

    class AcceptVersion extends Header {

        public AcceptVersion(String... versions) {
            super(Joiner.on(',').join(versions));
            this.versions = ImmutableList.copyOf(versions);
        }

        public AcceptVersion(String versions) {
            super(versions);
            this.versions = ImmutableList.copyOf(Splitter.on(',').split(versions));
        }

        public final ImmutableList<String> versions;
    }

    class Login extends Header {
        public Login(String value) { super(value); }
    }

    class Passcode extends Header {
        public Passcode(String value) { super(value); }
    }

    class Heartbeat extends Header {
        public Heartbeat(long x, long y) {
            super(Joiner.on(',').join(x, y));
            this.x = x;
            this.y = y;
        }

        public Heartbeat(String value) {
            super(value);
            Iterator<String> itr = Splitter.on(',').limit(2).split(value).iterator();
            x = Long.valueOf(itr.next());
            y = Long.valueOf(itr.next());
        }

        public final long x;
        public final long y;
    }

    class Version extends Header {
        public Version(String value) {
            super(value);
        }
    }

    class Host extends Header {
        public Host(String value) { super(value); }
    }

    class Destination extends Header {
        public Destination(String value) { super(value); }
    }

    class ReceiptId extends Header {
        public ReceiptId(String value) { super(value); }
    }

    class Receipt extends Header {
        public Receipt(String value) { super(value); }
    }

    class Transaction extends Header {
        public Transaction(String value) { super(value); }
    }

    class Ack extends Header {
        public Ack(String value) {
            super(value);
            type = toType(value);
        }

        public Ack(Type value) {
            super(value.toString());
            type = value;
        }

        private static Type toType(String value) {
            try {
                return Type.valueOf(Stompize.className(value));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public enum Type {
            Auto, Client, ClientIndividual;

            @Override
            public String toString() { return Stompize.headerName(super.toString()); }

        }

        public final Type type;
    }

    class MessageId extends Header {
        public MessageId(String value) { super(value); }
    }

    class Message extends Header {
        public Message(String value) { super(value); }
    }

    class Subscription extends Header {
        public Subscription(String value) { super(value); }
    }

    class Session extends Header {
        public Session(String value) { super(value); }
    }

    class Server extends Header {
        public Server(String value) {
            super(value);
            Iterator<String> itr = Splitter.on('/').omitEmptyStrings().split(value).iterator();
            name = itr.next();
            version = itr.next();
        }

        public Server(String name, String version) {
            super(Joiner.on('/').skipNulls().join(name, version));
            this.name = name;
            this.version = version;
        }

        public final String name;
        public final String version;
    }

    class Id extends Header {
        protected Id(String value) { super(value); }
    }


    class ContentType extends Header {
        protected ContentType(String value) { super(value); }
    }

    class ContentLength extends Header {
        protected ContentLength(String value) {
            super(value);
            this.value = Integer.valueOf(value);
        }

        protected ContentLength(int value) {
            super(String.valueOf(value));
            this.value = value;
        }

        public final int value;
    }
}