# Code as Specification.

Stompize is a java [STOMP][1] framework, it could help you build or enhance your [STOMP][1] application more easier and readable.

For example, suppose your [STOMP][1] specification has defined two command: `SEND` and `RECEIPT`, you can write java code like below without any document:

```java
public interface Stomp {
    void send(
            @Required("destination") String destination,
            @Optional("receipt") String receipt,
            Object content
    );

    void receipt(@Optional("receipt-id") String id);
}
```

Then, you can code the `StompClient`:

```java
public abstract class StompClient extends Specification implements Stomp {

    private final FrameOutput output;

    protected StompClient(FrameOutput output) { this.output = output; }

    @Override
    public void receipt(@Optional("receipt-id") String id) {
        System.out.println("received receipt: " + id);
    }

    @Override
    protected FrameOutput output() { return output; }

}
```

Caution, `StompClient` is `abstract`, because Stompize will help you override abstract methods declared at `Stomp`.

A simple unit test could let you figure it out:

```java
@Test
public void shouldStompizeASpec() throws Exception {
    FrameOutput output = mock(FrameOutput.class);
    StompClient stompClient = Stompize.newInstance(StompClient.class, output);

    stompClient.send("d", null, "c");

    verify(output).command("SEND");
    verify(output).header("destination", "d", true);
    verify(output).header("receipt", null, false);
    verify(output).content("c");
}

```

Easy right?! :)

This is just the start of building a real application, Stompize also provides lots of tools class to let you make it real fast.

Coming soon!

[1]:http://stomp.github.com//index.html