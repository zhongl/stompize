package com.github.zhongl.stompize;

import com.google.common.base.Function;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.objectweb.asm.MethodVisitor;

import static com.github.zhongl.stompize.Stomp.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class GenerateCommandOutTest {

    @Test
    public void shouldOutCommandFrame() throws Exception {
        Stomp stomp = new Driver<Stomp>(Stomp.class, CommandOut.class, "Suffix").apply().newInstance();

        stomp.disconnect();
        verify(FUNCTION).apply("[DISCONNECT, [], null]");

        stomp.send(new Destination("/a/b"), new Text("content"));
        verify(FUNCTION).apply("[SEND, [\ndestination:/a/b], \n\ncontent\u0000]");

        stomp.error(new Text("OMG"), new Message("failure"), new ReceiptId("1"));
        verify(FUNCTION).apply("[ERROR, [\nmessage:failure, \nreceipt-id:1], \n\nOMG\u0000]");
    }

    @Test
    public void shouldComplainUnexpectedArgumentType() throws Exception {
        try {
            new Driver<UnexpectedArgumentType>(UnexpectedArgumentType.class, UnexpectedArgumentTypeImpl.class, "Suffix").apply();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Unexpected argument type: java.lang.String"));
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
            visit(specification, new GenerateCommandOut<T>(cv, specification, aClass, self));
            super.visitEnd();
        }

        private final Class<T> specification;

    }

    abstract static class CommandOut extends Stompizeble implements Stomp {

        @Override
        protected void out(String command, Iterable<Header> headers, Content<?> content) {
            List<Object> list = Arrays.asList(command, headers, content);
            FUNCTION.apply(list.toString());
        }
    }

    interface UnexpectedArgumentType extends Specification {
        @Command
        void cmd(String value);
    }

    abstract static class UnexpectedArgumentTypeImpl extends Stompizeble implements UnexpectedArgumentType {}

    static final Function<String, Void> FUNCTION = mock(Function.class);

}
