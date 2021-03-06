package com.github.zhongl.stompize;


import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public interface Stomp extends Specification {

    @Command(optionals = {Login.class, Passcode.class, Heartbeat.class})
    void connect(AcceptVersion acceptVersion, Host host, Header... optionals);

    @Command(optionals = {Session.class, Server.class, Heartbeat.class})
    void connected(Version version, Header... optionals);

    @Command(optionals = {Transaction.class, Receipt.class})
    void send(Destination destination, Text content, Header... optionals);

    @Command
    void receipt(ReceiptId receiptId);

    @Command(optionals = Ack.class)
    void message(Destination destination, MessageId messageId, Subscription subscription, Text content, Header... optionals);

    @Command(optionals = {Message.class, ReceiptId.class})
    void error(Text content, Header... optionals);

    @Command(optionals = Receipt.class)
    void disconnect(Header... optionals);

    class Text extends Content<String> {

        public Text(Object value) { this(value, null); }

        public Text(Object value, String type) { super(value.toString(), type == null ? "text/plain" : type); }

        @Override
        protected String value() { return value; }
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
        public Heartbeat(int x, int y) {
            super(Joiner.on(',').join(x, y));
            this.x = x;
            this.y = y;
        }

        public Heartbeat(String value) {
            super(value);
            Iterator<String> itr = Splitter.on(',').limit(2).split(value).iterator();
            x = Integer.valueOf(itr.next());
            y = Integer.valueOf(itr.next());
        }

        public final int x;
        public final int y;
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
        public Ack(String value) { super(value); }
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

}
