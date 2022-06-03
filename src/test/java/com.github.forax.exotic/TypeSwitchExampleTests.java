package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class TypeSwitchExampleTests {
  private static final TypeSwitch TYPE_SWITCH = TypeSwitch.create(true, Integer.class, String.class);
  
  public static String asString(Object o) {
    switch(TYPE_SWITCH.typeSwitch(o)) {
    case TypeSwitch.NULL_MATCH:
      return "null";
    case 0:
      return "Integer";
    case 1:
      return "String";
    default: // TypeSwitch.BAD_MATCH
      return "unknown";
    }
  }
  
  @Test
  public void example() {
    assertEquals("null", asString(null));
    assertEquals("Integer", asString(3));
    assertEquals("String", asString("foo"));
    assertEquals("unknown", asString(4.5));
  }
}
