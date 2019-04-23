package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


@SuppressWarnings("static-method")
class ObjectSupportTests {
  static final class Hello {
    private static final ObjectSupport SUPPORT = ObjectSupport.of(lookup(), "name");
    
    private final String name;

    public Hello(String name) {
      this.name = name;
    }
    
    @Override
    public boolean equals(Object other) {
      return SUPPORT.equals(this, other);
    }
    
    @Override
    public int hashCode() {
      return SUPPORT.hashCode(this);
    }
  }
  
  @Test
  void testEqualsHello() {
    Hello hello1 = new Hello(new String("bonjour"));
    Hello hello2 = new Hello(new String("bonjour"));
    assertEquals(hello1, hello2);
  }
  
  @Test
  void testHashCodeHello() {
    Hello hello = new Hello("ola");
    assertEquals(1 * 31 + "ola".hashCode(), hello.hashCode());
  }
  
  
  
  static final class Foo {
    private static final ObjectSupport SUPPORT = ObjectSupport.of(lookup(), "a", "b", "c", "d", "e", "f", "g", "h", "s", "o");
    
    boolean a;
    byte b;
    short c;
    char d;
    int e;
    long f;
    float g;
    double h;
    String s;
    Object o;
    
    @Override
    public boolean equals(Object other) {
      return SUPPORT.equals(this, other);
    }
    
    @Override
    public int hashCode() {
      return SUPPORT.hashCode(this);
    }
  }
  
  @Test
  void testAllFieldsHashCode() {
    Foo foo1 = new Foo();
    Foo foo2 = new Foo();
    assertEquals(foo1.hashCode(), foo2.hashCode());
  }
  
  @Test
  void testAllFieldsHashCodeBoolean() {
    Foo foo1 = new Foo();
    foo1.a = true;
    Foo foo2 = new Foo();
    assertNotEquals(foo1.hashCode(), foo2.hashCode());
    foo2.a = true;
    assertEquals(foo1.hashCode(), foo2.hashCode());
  }
  @Test
  void testAllFieldsHashCodeByte() {
    Foo foo1 = new Foo();
    foo1.b = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1.hashCode(), foo2.hashCode());
    foo2.b = 1;
    assertEquals(foo1.hashCode(), foo2.hashCode());
  }
  @Test
  void testAllFieldsHashCodeShort() {
    Foo foo1 = new Foo();
    foo1.c = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1.hashCode(), foo2.hashCode());
    foo2.c = 1;
    assertEquals(foo1.hashCode(), foo2.hashCode());
  }
  @Test
  void testAllFieldsHashCodeChar() {
    Foo foo1 = new Foo();
    foo1.d = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1.hashCode(), foo2.hashCode());
    foo2.d = 1;
    assertEquals(foo1.hashCode(), foo2.hashCode());
  }
  @Test
  void testAllFieldsHashCodeInt() {
    Foo foo1 = new Foo();
    foo1.e = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1.hashCode(), foo2.hashCode());
    foo2.e = 1;
    assertEquals(foo1.hashCode(), foo2.hashCode());
  }
  @Test
  void testAllFieldsHashCodeLong() {
    Foo foo1 = new Foo();
    foo1.f = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1.hashCode(), foo2.hashCode());
    foo2.f = 1;
    assertEquals(foo1.hashCode(), foo2.hashCode());
  }
  @Test
  void testAllFieldsHashCodeFloat() {
    Foo foo1 = new Foo();
    foo1.g = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1.hashCode(), foo2.hashCode());
    foo2.g = 1;
    assertEquals(foo1.hashCode(), foo2.hashCode());
  }
  @Test
  void testAllFieldsHashCodeDouble() {
    Foo foo1 = new Foo();
    foo1.h = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1.hashCode(), foo2.hashCode());
    foo2.h = 1;
    assertEquals(foo1.hashCode(), foo2.hashCode());
  }
  @Test
  void testAllFieldsHashCodeString() {
    Foo foo1 = new Foo();
    foo1.s = new String("bar");
    Foo foo2 = new Foo();
    assertNotEquals(foo1.hashCode(), foo2.hashCode());
    foo2.s = new String("bar");
    assertEquals(foo1.hashCode(), foo2.hashCode());
  }
  @Test
  void testAllFieldsHashCodeObject() {
    Foo foo1 = new Foo();
    foo1.o = Integer.valueOf(1500);
    Foo foo2 = new Foo();
    assertNotEquals(foo1.hashCode(), foo2.hashCode());
    foo2.o = Integer.valueOf(1500);
    assertEquals(foo1.hashCode(), foo2.hashCode());
  }
  
  @Test
  void testAllFieldsEquals() {
    Foo foo1 = new Foo();
    Foo foo2 = new Foo();
    assertEquals(foo1, foo2);
  }
  
  @Test
  void testAllFieldsEqualsBoolean() {
    Foo foo1 = new Foo();
    foo1.a = true;
    Foo foo2 = new Foo();
    assertNotEquals(foo1, foo2);
    foo2.a = true;
    assertEquals(foo1, foo2);
  }
  @Test
  void testAllFieldsEqualsByte() {
    Foo foo1 = new Foo();
    foo1.b = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1, foo2);
    foo2.b = 1;
    assertEquals(foo1, foo2);
  }
  @Test
  void testAllFieldsEqualsShort() {
    Foo foo1 = new Foo();
    foo1.c = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1, foo2);
    foo2.c = 1;
    assertEquals(foo1, foo2);
  }
  @Test
  void testAllFieldsEqualsChar() {
    Foo foo1 = new Foo();
    foo1.d = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1, foo2);
    foo2.d = 1;
    assertEquals(foo1, foo2);
  }
  @Test
  void testAllFieldsEqualsInt() {
    Foo foo1 = new Foo();
    foo1.e = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1, foo2);
    foo2.e = 1;
    assertEquals(foo1, foo2);
  }
  @Test
  void testAllFieldsEqualsLong() {
    Foo foo1 = new Foo();
    foo1.f = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1, foo2);
    foo2.f = 1;
    assertEquals(foo1, foo2);
  }
  @Test
  void testAllFieldsEqualsFloat() {
    Foo foo1 = new Foo();
    foo1.g = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1, foo2);
    foo2.g = 1;
    assertEquals(foo1, foo2);
  }
  @Test
  void testAllFieldsEqualsDouble() {
    Foo foo1 = new Foo();
    foo1.h = 1;
    Foo foo2 = new Foo();
    assertNotEquals(foo1, foo2);
    foo2.h = 1;
    assertEquals(foo1, foo2);
  }
  @Test
  void testAllFieldsEqualsString() {
    Foo foo1 = new Foo();
    foo1.s = new String("bar");
    Foo foo2 = new Foo();
    assertNotEquals(foo1, foo2);
    foo2.s = new String("bar");
    assertEquals(foo1, foo2);
  }
  @Test
  void testAllFieldsEqualsObject() {
    Foo foo1 = new Foo();
    foo1.o = Integer.valueOf(1500);
    Foo foo2 = new Foo();
    assertNotEquals(foo1, foo2);
    foo2.o = Integer.valueOf(1500);
    assertEquals(foo1, foo2);
  }
  
  static final class Empty {
    static final ObjectSupport SUPPORT = ObjectSupport.of(lookup(), new String[0]);
    
    @Override
    public boolean equals(Object other) {
      return SUPPORT.equals(this, other);
    }
    
    @Override
    public int hashCode() {
      return SUPPORT.hashCode(this);
    }
  }
  
  
  @Test
  void testSelfEqualsContract() {
    assertAll(
      () -> assertThrows(NullPointerException.class, () -> Empty.SUPPORT.equals(null, new Empty())),
      () -> assertThrows(ClassCastException.class, () -> Empty.SUPPORT.equals(new Object(), null))
      );
  }
  
  @Test
  void testSelfHashCodeContract() {
    assertAll(
      () -> assertThrows(NullPointerException.class, () -> Empty.SUPPORT.hashCode(null)),
      () -> assertThrows(ClassCastException.class, () -> Empty.SUPPORT.hashCode(new Object()))
      );
  }
}
