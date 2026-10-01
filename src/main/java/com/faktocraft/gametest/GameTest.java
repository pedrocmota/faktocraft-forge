package com.faktocraft.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GameTest {
  String template();

  int timeoutTicks() default 100;

  int setupTicks() default 0;

  boolean required() default true;

  String batch() default "defaultBatch";
}
