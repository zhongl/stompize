package com.github.zhongl.stompize;

import com.google.common.base.Function;
import java.util.*;
import org.junit.Test;
import org.objectweb.asm.MethodVisitor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class GenerateStompizebleApplyTest {

    static final String SUFFIX = "Suffix";

    @Test
    public void shouldCallbackCommands() throws Exception {
        Class<Stomp> aClass = new Driver<Stomp>(Stomp.class, CallbackCommands.class, SUFFIX).apply();
        Stompizeble stompizeble = (Stompizeble) aClass.newInstance();

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("receipt-id", "1");

        stompizeble.apply("RECEIPT", headers, null);
        verify(FUNCTION).apply("\nreceipt-id:1");

        headers.clear();
        headers.put("destination", "/a/b");
        headers.put("message-id", "007");
        headers.put("subscription", "sub");
        headers.put("ack", "1");

        stompizeble.apply("MESSAGE", headers, "123");

        verify(FUNCTION).apply("[\ndestination:/a/b, \nmessage-id:007, \nsubscription:sub, \n\n123\u0000, \nack:1]");

        headers.clear();
        headers.put("message", "error");

        stompizeble.apply("ERROR", headers, "failure");
        verify(FUNCTION).apply("[\n\nfailure\u0000, \nmessage:error]");
    }

    @Test
    public void shouldComplainContentConstructor() throws Exception {
        try {
            IllegalSpec illegalSpec = new Driver<IllegalSpec>(IllegalSpec.class, IllegalCmd1.class, SUFFIX).apply().newInstance();
            ((Stompizeble) illegalSpec).apply("CMD1", Collections.<String, String>emptyMap(), "");
            fail();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("java.lang.NoSuchMethodException: com.github.zhongl.stompize.GenerateStompizebleApplyTest$IllegalContent.<init>(java.lang.Object, java.lang.String)"));
        }
    }

    @Test
    public void shouldComplainRequiredHeaderConstructor() throws Exception {
        try {
            IllegalSpec illegalSpec = new Driver<IllegalSpec>(IllegalSpec.class, IllegalCmd2.class, SUFFIX).apply().newInstance();
            ((Stompizeble) illegalSpec).apply("CMD2", Collections.singletonMap("illegal-header", ""), null);
            fail();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("java.lang.NoSuchMethodException: com.github.zhongl.stompize.GenerateStompizebleApplyTest$IllegalSpec$IllegalHeader.<init>(java.lang.String)"));
        }
    }

    @Test
    public void shouldComplainOptionalHeaderConstructor() throws Exception {
        try {
            IllegalSpec illegalSpec = new Driver<IllegalSpec>(IllegalSpec.class, IllegalCmd3.class, SUFFIX).apply().newInstance();
            ((Stompizeble) illegalSpec).apply("CMD3", Collections.singletonMap("illegal-header", ""), null);
            fail();
        } catch (StompizeException e) {
            assertThat(e.getMessage(), is("java.lang.NoSuchMethodException: com.github.zhongl.stompize.GenerateStompizebleApplyTest$IllegalSpec$IllegalHeader.<init>(java.lang.String)"));
        }
    }

    static class Driver<T extends Specification> extends GenerateSubClass<T> {

        Driver(Class<T> specification, Class<? extends T> aClass, String suffix) {
            super(aClass, suffix);
            this.specification = specification;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ("<init>".equals(name)) new GenerateConstructor(cv, parent, name, desc, signature, exceptions).apply();
            return null;
        }

        @Override
        public void visitEnd() {
            visit(Stompizeble.class, new GenerateStompizebleApply<T>(cv, specification, aClass, self));
            super.visitEnd();
        }

        private final Class<T> specification;

    }

    abstract static class CallbackCommands extends Stompizeble implements Stomp {

        @Override
        public void receipt(ReceiptId receiptId) { FUNCTION.apply(receiptId.toString());}

        @Override
        public void message(Destination destination, MessageId messageId, Subscription subscription, Text content, Header... optionals) {
            List<Object> list = Arrays.asList(destination, messageId, subscription, content, optionals[0]);
            FUNCTION.apply(list.toString());
        }

        @Override
        public void error(Text content, Header... optionals) {
            List<Object> list = Arrays.asList(content, optionals[0]);
            FUNCTION.apply(list.toString());
        }

    }

    interface IllegalSpec extends Specification {
        @Command
        void cmd1(IllegalContent content);

        @Command
        void cmd2(IllegalHeader header);

        @Command(optionals = IllegalHeader.class)
        void cmd3(Header... optional);

        class IllegalHeader extends Header {
            protected IllegalHeader() { super(""); }
        }
    }

    static class IllegalContent extends Content<Object> {

        public IllegalContent(Object value) {
            super(value, "");
        }

        @Override
        protected String value() { return value.toString(); }
    }

    abstract static class IllegalCmd1 extends Stompizeble implements IllegalSpec {
        @Override
        public void cmd1(IllegalContent content) { }
    }

    abstract static class IllegalCmd2 extends Stompizeble implements IllegalSpec {
        @Override
        public void cmd2(IllegalHeader header) { }
    }

    abstract static class IllegalCmd3 extends Stompizeble implements IllegalSpec {
        @Override
        public void cmd3(Header... optional) { }
    }

    final static Function<String, Void> FUNCTION = mock(Function.class);

}
