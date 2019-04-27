package com.github.forax.exotic;

import java.io.Serializable;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * Provide a fast implementation for {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * <p>
 * An {@code ObjectSupport} can be created either from a {@link Lookup}, the class containing the fields
 * and a set of lambdas accessing the fields using {@link ObjectSupport#of(Lookup, Class, ProjectionFunction...)}
 * or from a {@link Lookup}, the class containing the fields and a set of field names using
 * {@link ObjectSupport#of(Lookup, Class, String...)}.
 * <p>
 * The following example shows how to create and use a {@code ObjectSupport} configured
 * to use the fields {@code name} and {@code age}.
 * <pre>
 * class Person {
 *   private static final ObjectSupport&lt;Person&gt; SUPPORT = SUPPORT = ObjectSupport.of(lookup(), Person.class, p -&gt; p.name, p -&gt; p.age);
 *  
 *   private String name;
 *   private int age;
 *
 *   public Person(String name, int age) {
 *     this.name = name;
 *     this.age = age;
 *   }
 *   
 *   public boolean equals(Object other) {
 *     return SUPPORT.equals(this, other);
 *   }
 *   
 *   public int hashCode() {
 *     return SUPPORT.hashCode(this);
 *   }
 * }
 * </pre>
 * 
 * @param <T> the type of the class. 
 */
public interface ObjectSupport<T> {
  /**
   * Test if two object are equals.
   * 
   * @param self an instance of the class used to create the current {@code ObjectSupport}.
   * @param other any instance or null.
   * @return true if the two objects are equals.
   * @throws NullPointerException if {@code self} is null.
   * @throws ClassCastException if {@code self} is not an instance of the class
   *   used to create the current {@code ObjectSupport}.
   * @see Object#equals(Object)
   */
  public abstract boolean equals(T self, Object other);
  
  /**
   * Return a hash value of an instance of the class used to create the current {@code ObjectSupport}.
   * 
   * @param self an instance of the class used to create the current {@code ObjectSupport}.
   * @return a hash value of {@code self}.
   * @throws NullPointerException if {@code self} is null.
   * @throws ClassCastException if {@code self} is not an instance of the class
   *   used to create the current {@code ObjectSupport}.
   * @see Object#hashCode()
   */
  public abstract int hashCode(T self);
  
  /**
   * Return an object support from a lookup object and some field names.
   * 
   * @param <T> the type of the class.
   * @param lookup a lookup with enough access rights to see the class fields.
   * @param type the class containing the fields.
   * @param fieldNames names of the fields that will be use for the computations.
   * @return a new fresh object support. This object should always be stored in a {@code static} {@code final} field.
   * @throws NullPointerException if {@code lookup} is null, {@code type} is null or the array of field is null.
   */
  public static <T> ObjectSupport<T> of(Lookup lookup, Class<T> type, String... fieldNames) {
    return ObjectSupports.createUsingFieldNames(lookup, type, fieldNames);
  }
  
  /**
   * Return an object support from a lookup object and a function that does reflection to find the fields.
   * 
   * @param <T> the type of the class.
   * @param lookup a lookup with enough access rights to see the class fields.
   * @param type the class containing the fields.
   * @param transformer a function that map the class to the fields used for the subsequent computations.
   * @return a new fresh object support. This object should always be stored in a {@code static} {@code final} field.
   * @throws NullPointerException if {@code lookup} is null, {@code type} is null or {@code transformer} is null.
   */
  public static <T> ObjectSupport<T> ofReflection(Lookup lookup, Class<T> type, Function<? super Class<T>, ? extends Field[]> transformer) {
    return ObjectSupports.createUsingReflectFields(lookup, type, transformer);
  }
  
  /**
   * A function that retrieve the value of a field of the class.
   *
   * @param <T> type of the parameter.
   * @param <R> type of the return value.
   * 
   * @see ObjectSupport#of(Lookup, Class, ProjectionFunction...)
   */
  @FunctionalInterface
  public interface ProjectionFunction<T, R> extends Function<T, R>, Serializable, ObjectSupportProjections.ProjectionDeserializer {
    // empty
  }
  
  /**
   * Return an object support from a lookup object and lambdas returning the fields.
   * 
   * @param <T> the type of the class.
   * @param lookup a lookup with enough access rights to see the class fields.
   * @param type the class containing the fields.
   * @param projections lambdas that returns the value of a field of the class.
   * @return a new fresh object support. This object should always be stored in a {@code static} {@code final} field.
   * @throws NullPointerException if {@code lookup} is null, {@code type} is null or {@code transformer} is null.
   * @throws IllegalArgumentException if the code of the lambdas is not accessible from the lookup object or
   *   if one lambda doesn't do a field access.
   */
  @SafeVarargs
  public static <T> ObjectSupport<T> of(Lookup lookup, Class<T> type, ProjectionFunction<? super T, ?>... projections) {
    return ObjectSupports.createUsingLambdas(lookup, type, projections);
  }
}
