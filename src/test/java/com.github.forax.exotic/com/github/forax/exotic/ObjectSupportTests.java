package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.jupiter.api.Test;


@SuppressWarnings("static-method")
class ObjectSupportTests {
  static final class Hello {
    private static final ObjectSupport<Hello> SUPPORT = ObjectSupport.of(lookup(), Hello.class, "name");
    
    @SuppressWarnings("unused")
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
    private static final ObjectSupport<Foo> SUPPORT = ObjectSupport.of(lookup(), Foo.class, "a", "b", "c", "d", "e", "f", "g", "h", "s", "o");
    
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
    @SuppressWarnings("unchecked")
    static final ObjectSupport<Object> SUPPORT = ObjectSupport.of(lookup(), (Class<Object>)(Class<?>)Empty.class, new String[0]);
    
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
  
  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface ObjectSupportField {
    // empty
  }
  
  static Field[] findAnnotatedFields(Class<?> type) {
    return Arrays.stream(type.getDeclaredFields()).filter(field -> field.isAnnotationPresent(ObjectSupportField.class)).toArray(Field[]::new);
  }
  
  static class User {
    private static final ObjectSupport<User> SUPPORT = ObjectSupport.ofReflection(lookup(), User.class, ObjectSupportTests::findAnnotatedFields);
    
    @ObjectSupportField
    String name;
    @ObjectSupportField
    boolean vip;
    
    public User(String name, boolean vip) {
      this.name = name;
      this.vip = vip;
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
  void testEqualsUser() {
    User user1 = new User("bob", true);
    User user2 = new User("bob", true);
    assertEquals(user1, user2);
  }
  @Test
  void testNotEqualsUser() {
    User user1 = new User("bob", true);
    User user2 = new User("bob", false);
    assertNotEquals(user1, user2);
  }
  
  @Test
  void testHashCodeUser() {
    User user1 = new User("bob", true);
    User user2 = new User("bob", true);
    assertEquals(user1.hashCode(), user2.hashCode());
  }
  @Test
  void testHashCodeNotEqualsUser() {
    User user1 = new User("bob", true);
    User user2 = new User("bob", false);
    assertNotEquals(user1.hashCode(), user2.hashCode());
  }
  
  static class Point {
    private static final ObjectSupport<Point> SUPPORT;
    static {
      try {
        SUPPORT = ObjectSupport.of(lookup(), Point.class, p -> p.x, p -> p.y);
      } catch(Throwable t) {
        t.printStackTrace();
        throw t;
      }
    }
    
    int x;
    int y;
    
    public Point(int x, int y) {
      this.x = x;
      this.y = y;
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
  void testEqualsPoint() {
    Point p1 = new Point(1, 2);
    Point p2 = new Point(1, 2);
    assertEquals(p1, p2);
  }
  
  @Test
  void testHashCodePoint() {
    Point p1 = new Point(1, 2);
    Point p2 = new Point(1, 2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }
  
  static class Color {
    private static final ObjectSupport<Color> SUPPORT;
    static {
      try {
        SUPPORT = ObjectSupport.of(lookup(), Color.class, c -> c.name, c -> c.light);
      } catch(Throwable t) {
        t.printStackTrace();
        throw t;
      }
    }
    
    String name;
    boolean light;
    
    public Color(String name, boolean light) {
      this.name = name;
      this.light = light;
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
  void testEqualsColor() {
    Color c1 = new Color("red", true);
    Color c2 = new Color("red", true);
    assertEquals(c1, c2);
  }
  
  @Test
  void testHashCodeColor() {
    Color c1 = new Color("red", true);
    Color c2 = new Color("red", true);
    assertEquals(c1.hashCode(), c2.hashCode());
  }
}
