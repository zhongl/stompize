package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public interface V1_2 extends Specification {

    void connect(
            @Required("accept-version") String acceptVersion,
            @Required("host") String host,
            @Optional("login") String login,
            @Optional("passcode") String passcode,
            @Optional("heart-beat") String heartBeat
    );

    void stomp(
            @Required("accept-version") String acceptVersion,
            @Required("host") String host,
            @Optional("login") String login,
            @Optional("passcode") String passcode,
            @Optional("heart-beat") String heartBeat
    );

    void connected(
            @Required("version") String version,
            @Optional("session") String session,
            @Optional("server") String server,
            @Optional("heart-beat") String heartBeat
    );

    void send(
            @Required("destination") String destination,
            @Optional("transaction") String transaction,
            @Optional("receipt") String receipt,
            Content content
    );

    void subscribe(
            @Required("destination") String destination,
            @Required("id") String id,
            @Optional("receipt") String receipt,
            @Optional("ack") String ack
    );

    void unsubscribe(
            @Required("id") String id,
            @Optional("receipt") String receipt
    );

    void ack(
            @Required("id") String id,
            @Optional("receipt") String receipt,
            @Optional("transaction") String transaction
    );

    void nack(
            @Required("id") String id,
            @Optional("receipt") String receipt,
            @Optional("transaction") String transaction
    );

    void begin(
            @Required("transaction") String transaction,
            @Optional("receipt") String receipt
    );

    void commit(
            @Required("transaction") String transaction,
            @Optional("receipt") String receipt
    );

    void abort(
            @Required("transaction") String transaction,
            @Optional("receipt") String receipt
    );

    void disconnect(@Optional("receipt") String receipt);

    void message(
            @Required("destination") String destination,
            @Required("message-id") String messageId,
            @Required("subscription") String subscription,
            @Optional("ack") String ack,
            Content content
    );

    void receipt(@Required("receipt-id") String receiptId);

    void error(
            @Optional("message") String message,
            Content content
    );
}
