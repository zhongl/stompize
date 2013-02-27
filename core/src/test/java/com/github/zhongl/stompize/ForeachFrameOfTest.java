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
            protected void header(String name, int index, boolean required) {
                if ("required".equals(name)) {
                    assertThat(required, is(true));
                    assertThat(index, is(1));
                } else {
                    assertThat(required, is(false));
                    assertThat(index, is(2));
                }
            }

            @Override
            protected boolean exclude(Method method) {
                return !Modifier.isAbstract(method.getModifiers());
            }

            @Override
            protected void command(String name, Method method) {
                assertThat(name, is("command"));
            }

            @Override
            protected void content(int index) {
                assertThat(index, is(3));
            }
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
            protected void header(String name, int index, boolean required) { }

            @Override
            protected void content(int index) {
                assertThat(index, is(-1));
            }
        }.apply();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainContentIsNotLast() throws Exception {
        new ForeachFrameOf(ContentIsNotLast.class) {

            @Override
            protected boolean exclude(Method method) {
                return !Modifier.isAbstract(method.getModifiers());
            }

            @Override
            protected void command(String name, Method method) { }

            @Override
            protected void header(String name, int index, boolean required) { }

            @Override
            protected void content(int index) { }
        }.apply();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainContentIsPrimitive() throws Exception {
        new ForeachFrameOf(ContentIsPrimitive.class) {

            @Override
            protected boolean exclude(Method method) {
                return !Modifier.isAbstract(method.getModifiers());
            }

            @Override
            protected void command(String name, Method method) { }

            @Override
            protected void header(String name, int index, boolean required) { }

            @Override
            protected void content(int index) { }
        }.apply();
    }

    static abstract class CommandWithContent extends Specification {
        public abstract void command(@Required("required") String required, @Optional("optional") String optional, String content);
    }

    static abstract class CommandWithOutContent extends Specification {
        public abstract void command(@Required("required") String required, @Optional("optional") String optional);
    }

    static abstract class ContentIsNotLast extends Specification {
        public abstract void command(@Required("required") String required, String content, @Optional("optional") String optional);
    }

    static abstract class ContentIsPrimitive extends Specification {
        public abstract void command(@Required("required") String required, @Optional("optional") String optional, int content);
    }

}
