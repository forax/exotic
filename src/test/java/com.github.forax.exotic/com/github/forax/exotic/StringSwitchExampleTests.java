package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class StringSwitchExampleTests {
  private static final StringSwitch STRING_SWITCH = StringSwitch.create(true, "bernie the dog", "zara the cat");
  
  public static String owner(String s) {
    switch(STRING_SWITCH.stringSwitch(s)) {
    case StringSwitch.NULL_MATCH:
      return "no owner";
    case 0:
      return "john";
    case 1:
      return "jane";
    default: // TypeSwitch.BAD_MATCH
      return "unknown owner";
    }
  }
  
  @Test
  void example() {
    assertEquals("no owner", owner(null));
    assertEquals("john", owner("bernie the dog"));
    assertEquals("jane", owner("zara the cat"));
    assertEquals("unknown owner", owner("foo"));
  }
}
