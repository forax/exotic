package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class TypeSwitchTests {
  @Test
  public void simple() {
    TypeSwitch typeSwitch = TypeSwitch.create(false, Integer.class, String.class);
    assertAll( 
        () -> assertEquals(0, typeSwitch.typeSwitch(3)),
        () -> assertEquals(0, typeSwitch.typeSwitch(42)),
        () -> assertEquals(1, typeSwitch.typeSwitch("foo")),
        () -> assertEquals(1, typeSwitch.typeSwitch("bar")),
        () -> assertEquals(TypeSwitch.NO_MATCH, typeSwitch.typeSwitch(4.5))
      );
  }
  
  @Test
  public void inheritance() {
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
  public void interfaces() {
    TypeSwitch typeSwitch = TypeSwitch.create(false, I.class, J.class);
    assertAll(
        () -> assertEquals(0, typeSwitch.typeSwitch(new A())),
        () -> assertEquals(0, typeSwitch.typeSwitch(new A())),
        () -> assertEquals(TypeSwitch.NO_MATCH, typeSwitch.typeSwitch("bar"))
      );
  }
  
  @Test
  public void interfaces2() {
    TypeSwitch typeSwitch = TypeSwitch.create(false, J.class, I.class);
    assertAll(
        () -> assertEquals(0, typeSwitch.typeSwitch(new A())),
        () -> assertEquals(0, typeSwitch.typeSwitch(new A())),
        () -> assertEquals(TypeSwitch.NO_MATCH, typeSwitch.typeSwitch("bar"))
      );
  }
  
  @Test
  public void nonNullSwitchCalledWithANull() {
    TypeSwitch typeSwitch = TypeSwitch.create(false);
    assertThrows(NullPointerException.class, () -> typeSwitch.typeSwitch(null));
  }
  
  @Test
  public void nullCase() {
    TypeSwitch typeSwitch = TypeSwitch.create(true, String.class);
    assertAll(
        () -> assertEquals(0, typeSwitch.typeSwitch("foo")),
        () -> assertEquals(TypeSwitch.NULL_MATCH, typeSwitch.typeSwitch(null)),
        () -> assertEquals(TypeSwitch.NO_MATCH, typeSwitch.typeSwitch(3))
      );
  }
  
  @Test
  public void aCaseCanNotBeNull() {
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> TypeSwitch.create(false, (Class<?>)null)),
        () -> assertThrows(NullPointerException.class, () -> TypeSwitch.create(true, (Class<?>)null))
      );
  }
  
  @Test
  public void invalidPartialOrder() {
    assertAll(
        () -> assertThrows(IllegalStateException.class, () -> TypeSwitch.create(false, Object.class, String.class)),
        () -> assertThrows(IllegalStateException.class, () -> TypeSwitch.create(false, Comparable.class, String.class)),
        () -> assertThrows(IllegalStateException.class, () -> TypeSwitch.create(false, Object.class, Comparable.class)),
        () -> assertThrows(IllegalStateException.class, () -> TypeSwitch.create(false, Serializable.class, Comparable.class, String.class))
      );
  }
}
