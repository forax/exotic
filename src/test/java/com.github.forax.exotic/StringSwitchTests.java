package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class StringSwitchTests {
  @Test
  public void simple() {
    StringSwitch stringSwitch = StringSwitch.create(false, "foo", "bar");
    assertAll( 
        () -> assertEquals(0, stringSwitch.stringSwitch("foo")),
        () -> assertEquals(0, stringSwitch.stringSwitch(new String("foo"))),
        () -> assertEquals(1, stringSwitch.stringSwitch("bar")),
        () -> assertEquals(1, stringSwitch.stringSwitch(new String("bar"))),
        () -> assertEquals(StringSwitch.NO_MATCH, stringSwitch.stringSwitch("baz"))
      );
  }
  
  @Test
  public void nonNullSwitchCalledWithANull() {
    StringSwitch stringSwitch = StringSwitch.create(false);
    assertThrows(NullPointerException.class, () -> stringSwitch.stringSwitch(null));
  }
  
  @Test
  public void nullCase() {
    StringSwitch stringSwitch = StringSwitch.create(true, "foo");
    assertAll(
        () -> assertEquals(0, stringSwitch.stringSwitch("foo")),
        () -> assertEquals(StringSwitch.NULL_MATCH, stringSwitch.stringSwitch(null)),
        () -> assertEquals(StringSwitch.NO_MATCH, stringSwitch.stringSwitch(""))
      );
  }
  
  @Test
  public void aCaseCanNotBeNull() {
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> StringSwitch.create(false, (String)null)),
        () -> assertThrows(NullPointerException.class, () -> StringSwitch.create(true, (String)null))
      );
  }
  
  @Test
  public void casesArrayCanNotBeNull() {
    assertThrows(NullPointerException.class, () -> StringSwitch.create(false, (String[])null));
  }
}
