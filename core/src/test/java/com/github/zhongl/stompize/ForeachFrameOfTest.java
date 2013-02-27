package com.github.zhongl.stompize;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class ForeachFrameOfTest {

    @Test
    public void shouldVisitAbstraceMethodOfCommandWithContent() throws Exception {
        new ForeachFrameOf(CommandWithContent.class) {

            @Override
            protected boolean exclude(Method method) {
                return !Modifier.isAbstract(method.getModifiers());
            }

            @Override
            protected void command(String name, Method method) {
                assertThat(name, is("command"));
            }

            @Override
            protected void required(String headerName, int index) {
                assertThat(headerName, is("simple"));
                assertThat(index, is(1));
            }

            @Override
            protected void content(int index) {
                assertThat(index, is(2));
            }

            @Override
            protected void optionals(int index) {
                assertThat(index, is(3));
            }
        }.apply();
    }

    @Test
    public void shouldSupportCamelNameToDashedLowCase() throws Exception {
        new ForeachFrameOf(CommandWithCamelName.class) {

            @Override
            protected boolean exclude(Method method) {
                return !Modifier.isAbstract(method.getModifiers());
            }

            @Override
            protected void command(String name, Method method) { }

            @Override
            protected void required(String headerName, int index) {
                assertThat(headerName, is("camel-name"));
                assertThat(index, is(1));
            }

            @Override
            protected void content(int index) { }

            @Override
            protected void optionals(int index) { }
        }.apply();
    }

    @Test
    public void shouldVisitAbstraceMethodOfCommandWithOutContent() throws Exception {
        new ForeachFrameOf(CommandWithOutContent.class) {

            @Override
            protected boolean exclude(Method method) {
                return !Modifier.isAbstract(method.getModifiers());
            }

            @Override
            protected void command(String name, Method method) { }

            @Override
            protected void required(String headerName, int index) { }


            @Override
            protected void content(int index) {
                assertThat(index, is(-1));
            }

            @Override
            protected void optionals(int index) { }
        }.apply();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainUnexpectArgumentType() throws Exception {
        new ForeachFrameOf(CommandWithNetherHeaderNorContent.class) {

            @Override
            protected boolean exclude(Method method) {
                return !Modifier.isAbstract(method.getModifiers());
            }

            @Override
            protected void command(String name, Method method) { }

            @Override
            protected void required(String headerName, int index) { }

            @Override
            protected void content(int index) { }

            @Override
            protected void optionals(int index) { }
        }.apply();
    }

    static abstract class CommandWithContent {
        public abstract void command(Simple simple, Plain content, Header... optionals);
    }

    static abstract class CommandWithOutContent {
        public abstract void command(Simple simple, Header... optionals);
    }


    static abstract class CommandWithCamelName {
        public abstract void command(CamelName camelName);
    }

    static abstract class CommandWithNetherHeaderNorContent {
        public abstract void command(NetherHeaderNorContent netherHeaderNorContent);
    }

    static class Simple extends Header {
        protected Simple(String value) { super(value); }
    }

    static class CamelName extends Header {
        protected CamelName(String value) { super(value); }
    }

    static class Plain extends Content<String> {
        protected Plain(String value) { super(value, ""); }

        @Override
        protected String value() { return value; }
    }

    static class NetherHeaderNorContent {}

}
