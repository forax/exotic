package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class StructuralCallExample2Tests {
  private static final StructuralCall FOO =
      StructuralCall.create(lookup(), "foo", methodType(int.class, String.class));

  static class A {
    int foo(String s) {
      return s.length() * 1;
    }
  }

  static class B {
    int foo(String s) {
      return s.length() * 2;
    }
  }

  static int foo(Object o, String s) {
    return FOO.invoke(o, s);
  }

  @Test
  void test() {
    assertEquals(5, foo(new A(), "hello"));
    assertEquals(30, foo(new B(), "structural call"));
  }
}
