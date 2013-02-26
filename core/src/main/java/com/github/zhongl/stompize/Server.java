package com.github.zhongl.stompize;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Server {}
