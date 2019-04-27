package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class ObjectSupportExampleTests {
  static final class Person {
    private static final ObjectSupport<Person> SUPPORT = ObjectSupport.of(lookup(), Person.class, p -> p.name, p -> p.age);
    
    private final String name;
    private final int age;

    public Person(String name, int age) {
      this.name = name;
      this.age = age;
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
