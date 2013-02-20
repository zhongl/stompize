package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class DemoClient implements Demo {
    @Override
    public void receipt(@Required("receipt-id") String receiptId) {
        System.out.println("[C] < RECEIPT: " + receiptId);
    }
}
