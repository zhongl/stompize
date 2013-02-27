package com.github.zhongl.stompize;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class GenerateStompizedClassTest {

    @Test
    public void shouldComplainIllegalAbstractMethod() throws Exception {
        try {
            new GenerateStompizedClass<Stomp>(Stomp.class, IllegalAbstractMethod.class, SUFFIX).apply();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Abstract method declared."));
        }
    }

    @Test
    public void shouldComplainNotStompizeble() throws Exception {
        try {
            new GenerateStompizedClass<Stomp>(Stomp.class, Unstompizeble.class, SUFFIX).apply();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is(Unstompizeble.class + " should extend " + Stompizeble.class + '.'));
        }
    }

    @Test
    public void shouldComplainSpecificationIsNotInterface() throws Exception {
        try {
            new GenerateStompizedClass<Noninterface>(Noninterface.class, NoninterfaceImpl.class, SUFFIX).apply();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is(Noninterface.class + " should be a interface."));
        }
    }

    @Test
    public void shouldComplainMissingRequiredHeader() throws Exception {
        Class<Stomp> aClass = new GenerateStompizedClass<Stomp>(Stomp.class, MissingRequiredHeader.class, SUFFIX).apply();
        try {
            aClass.newInstance().receipt(null);
            fail();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("Missing required header: receipt-id"));
        }
    }

    @Test
    public void shouldNotCheckContentAndOptionals() throws Exception {
        Class<Stomp> aClass = new GenerateStompizedClass<Stomp>(Stomp.class, DoNotCheckContentAndOptionals.class, SUFFIX).apply();
        aClass.newInstance().error(null, null);
    }

    abstract static class IllegalAbstractMethod extends Stompizeble implements Stomp {

        public abstract void m();
    }

    abstract static class Unstompizeble implements Stomp {}

    abstract static class Noninterface implements Specification {}

    abstract static class NoninterfaceImpl extends Noninterface {}

    abstract static class MissingRequiredHeader extends Stompizeble implements Stomp {

        @Override
        public void receipt(ReceiptId receiptId) { }

    }

    abstract static class DoNotCheckContentAndOptionals extends Stompizeble implements Stomp {
        @Override
        public void error(Text content, Header... optionals) { }
    }

    static final String SUFFIX = "suffix";
}
