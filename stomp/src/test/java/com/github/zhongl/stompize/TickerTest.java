package com.github.zhongl.stompize;

import org.junit.Test;

import static org.mockito.Mockito.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class TickerTest {

    @Test
    public void shouldAutoTickOut() throws Exception {
        Ticker ticker = new Ticker(50, tickOutCallback, 0, deadInCallback);
        ticker.start();
        Thread.sleep(130L);
        ticker.stop();

        verify(tickOutCallback, times(2)).run();
        verify(deadInCallback, never()).run();
    }

    @Test
    public void shouldNotTickOut() throws Exception {
        Ticker ticker = new Ticker(50, tickOutCallback, 0, deadInCallback);
        ticker.start();
        ticker.tickOut();
        Thread.sleep(90L);
        ticker.stop();

        verify(tickOutCallback, never()).run();
    }

    @Test
    public void shouldDeadIn() throws Exception {
        Ticker ticker = new Ticker(0, tickOutCallback, 50, deadInCallback);
        ticker.start();
        Thread.sleep(90L);
        ticker.stop();

        verify(deadInCallback).run();
        verify(tickOutCallback, never()).run();
    }

    @Test
    public void shouldNeverDeadInTwice() throws Exception {
        Ticker ticker = new Ticker(0, tickOutCallback, 50, deadInCallback);
        ticker.start();
        Thread.sleep(130L);
        ticker.stop();

        verify(deadInCallback, times(1)).run();
    }

    @Test
    public void shouldNotDeadIn() throws Exception {
        Ticker ticker = new Ticker(0, tickOutCallback, 50, deadInCallback);
        ticker.start();
        ticker.tickIn();
        Thread.sleep(90L);
        ticker.stop();

        verify(deadInCallback, never()).run();
    }

    Runnable tickOutCallback = mock(Runnable.class);
    Runnable deadInCallback  = mock(Runnable.class);
}
