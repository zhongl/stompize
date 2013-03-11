# Code as Specification.

Stompize is a java [STOMP][1] framework, it could help you build or enhance your [STOMP][1] application more easier and readable.

For example, suppose your [STOMP][1] specification has defined two command: `SEND` and `RECEIPT`, you can write java code like below without any document:

```java
public interface Stomp extends Specification {
    @Command(optionals = {Transaction.class, Receipt.class})
    void send(Destination destination, Text content, Header... optionals);

    @Command
    void receipt(ReceiptId receiptId);

    class Text extends Content<String> { ... }

    class Destination extends Header { ... }

    class Transaction extends Header { ... }

    class Receipt extends Header { ... }

    class ReceiptId extends Header { ... }
}
```

Then, you can code the `Client`:

```java
public abstract class Client extends Stompizeble implements Stomp {

    private final Function channel; // channel to communicate with server.

    protected Client(Function channel) { this.channel = channel; }

    @Override
    public void receipt(@Optional("receipt-id") String id) {
        channel.apply(join("RECEIPT", receiptId)); // receive receipt from server.
    }

    @Override
    protected  void out(String command, Iterable<Header> headers, Content<?> content) {
        channel.apply(...); // send a frame to server
    }

}
```

Caution, `Client` is `abstract`, because Stompize will help you override abstract methods declared at `Stomp`, eg: send(...).

A simple unit test could let you figure it out:

```java
    @Test
    public void shouldOutputFrameWithContent() throws Exception {
        Function<String, Void> output = mock(Function.class);
        Stomp client = Stompize.create(Stomp.class, Client.class, output);

        Destination d = new Destination("d");
        client.send(d, new Stomp.Text("content"));

        verify(output).apply("[SEND, [[\ndestination:d], \n\ncontent\u0000]]");
    }

    @Test
    public void shouldCallbackCommand() throws Exception {
        Function<String, Void> output = mock(Function.class);
        Stompizeble stompizeble = (Stompizeble) Stompize.create(Stomp.class, Client.class, output);

        stompizeble.apply("RECEIPT", Collections.singletonMap("receipt-id","1"), null);

        verify(output).apply("[RECEIPT, 1]");
    }

```

Easy right?! So let's stompize your application!

You can get more help from unit test cases:

- [core](https://github.com/zhongl/stompize/tree/master/core/src/test/java/com/github/zhongl/stompize)
- [stomp](https://github.com/zhongl/stompize/tree/master/stomp/src/test/java/com/github/zhongl/stompize)

[1]:http://stomp.github.com/index.html