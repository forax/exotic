package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class ObjectSupportExampleTests {
  static final class Person {
    private static final BiPredicate<Object, Object> EQUALS;
    private static final ToIntFunction<Object> HASH_CODE;
    static {
      ObjectSupport support = ObjectSupport.of(lookup(), "name", "age");
      EQUALS = support.getEquals();
      HASH_CODE = support.getHashCode();
    }
    
    private final String name;
    private final int age;

    public Person(String name, int age) {
      this.name = name;
      this.age = age;
    }
    
    @Override
    public boolean equals(Object other) {
      return EQUALS.test(this, other);
    }
    
    @Override
    public int hashCode() {
      return HASH_CODE.applyAsInt(this);
    }
  }
  
  @Test
  void testEqualsPerson() {
    Person person1 = new Person("bob", 34);
    Person person2 = new Person("cathy", 27);
    Person person3 = new Person("bob", 34);
    assertNotEquals(person1, person2);
    assertEquals(person1, person3);
  }
  
  @Test
  void testHashCodePerson() {
    Person person1 = new Person("cathy", 27);
    Person person2 = new Person("cathy", 27);
    assertEquals(person1.hashCode(), person2.hashCode());
  }
}
