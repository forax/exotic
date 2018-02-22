package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class StructuralCallExampleTests {
  private final static StructuralCall IS_EMPTY =
      StructuralCall.create(MethodHandles.lookup(), "isEmpty", MethodType.methodType(boolean.class));
  
  static boolean isEmpty(Object o) {
    return IS_EMPTY.invoke(o);
  }
  
  @Test
  void test() {
    assertTrue(isEmpty(List.of()));
    assertFalse(isEmpty(List.of(1)));
    assertTrue(isEmpty(Set.of()));
    assertTrue(isEmpty(Map.of()));
    assertTrue(isEmpty(""));
  }
}
