package com.github.zhongl.stompize;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.concurrent.TimeUnit;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class Ticker {

    public Ticker(long outMillis, Runnable tickOutCallback, long inMillis, Runnable deadInCallback) {
        out = outMillis;
        in = inMillis;
        this.tickOutCallback = tickOutCallback;
        this.deadInCallback = deadInCallback;
        timer = new HashedWheelTimer(1, TimeUnit.MILLISECONDS);
    }


    private final long             out;
    private final Runnable         tickOutCallback;
    private final long             in;
    private final Runnable         deadInCallback;
    private final HashedWheelTimer timer;

    private volatile boolean flagOut;
    private volatile boolean flagIn;

    public void tickIn() { flagIn = true; }

    public void tickOut() { flagOut = true; }

    public void start() {
        if (out > 0) outTick();
        if (in > 0) inDead();
    }

    public void stop() { timer.stop(); }

    private void inDead() {
        try {
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    if (!flagIn) deadInCallback.run();
                    else {
                        flagIn = false;
                        inDead();
                    }
                }
            }, in, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException ignore) { }
    }

    private void outTick() {
        try {
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    if (!flagOut) {
                        tickOutCallback.run();
                    } else {
                        flagOut = false;
                    }
                    outTick();
                }
            }, out, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException ignore) { }
    }
}
