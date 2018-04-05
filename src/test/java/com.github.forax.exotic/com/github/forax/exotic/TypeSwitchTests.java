package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class TypeSwitchTests {
  @Test
  void simple() {
    TypeSwitch typeSwitch = TypeSwitch.create(false, Integer.class, String.class);
    assertAll( 
        () -> assertEquals(0, typeSwitch.typeSwitch(3)),
        () -> assertEquals(0, typeSwitch.typeSwitch(42)),
        () -> assertEquals(1, typeSwitch.typeSwitch("foo")),
        () -> assertEquals(1, typeSwitch.typeSwitch("bar")),
        () -> assertEquals(TypeSwitch.BAD_MATCH, typeSwitch.typeSwitch(4.5))
      );
  }
  
  @Test
  void inheritance() {
    TypeSwitch typeSwitch = TypeSwitch.create(false, CharSequence.class, Object.class);
    assertAll(
        () -> assertEquals(1, typeSwitch.typeSwitch(3)),
        () -> assertEquals(1, typeSwitch.typeSwitch(42)),
        () -> assertEquals(0, typeSwitch.typeSwitch("foo")),
        () -> assertEquals(0, typeSwitch.typeSwitch("bar")),
        () -> assertEquals(1, typeSwitch.typeSwitch(4.5))
      );
  }
  
  interface I { /*empty*/ }
  interface J { /*empty*/ }
  class A implements I, J { /*empty*/ }
  
  @Test
  void interfaces() {
    TypeSwitch typeSwitch = TypeSwitch.create(false, I.class, J.class);
    assertAll(
        () -> assertEquals(0, typeSwitch.typeSwitch(new A())),
        () -> assertEquals(0, typeSwitch.typeSwitch(new A())),
        () -> assertEquals(TypeSwitch.BAD_MATCH, typeSwitch.typeSwitch("bar"))
      );
  }
  
  @Test
  void interfaces2() {
    TypeSwitch typeSwitch = TypeSwitch.create(false, J.class, I.class);
    assertAll(
        () -> assertEquals(0, typeSwitch.typeSwitch(new A())),
        () -> assertEquals(0, typeSwitch.typeSwitch(new A())),
        () -> assertEquals(TypeSwitch.BAD_MATCH, typeSwitch.typeSwitch("bar"))
      );
  }
  
  @Test
  void nonNullSwitchCalledWithANull() {
    TypeSwitch typeSwitch = TypeSwitch.create(false);
    assertThrows(NullPointerException.class, () -> typeSwitch.typeSwitch(null));
  }
  
  @Test
  void nullCase() {
    TypeSwitch typeSwitch = TypeSwitch.create(true, String.class);
    assertAll(
        () -> assertEquals(0, typeSwitch.typeSwitch("foo")),
        () -> assertEquals(TypeSwitch.NULL_MATCH, typeSwitch.typeSwitch(null)),
        () -> assertEquals(TypeSwitch.BAD_MATCH, typeSwitch.typeSwitch(3))
      );
  }
  
  @Test
  void aCaseCanNotBeNull() {
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> TypeSwitch.create(false, (Class<?>)null)),
        () -> assertThrows(NullPointerException.class, () -> TypeSwitch.create(true, (Class<?>)null))
      );
  }
}
