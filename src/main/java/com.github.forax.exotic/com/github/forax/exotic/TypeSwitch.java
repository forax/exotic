package com.github.forax.exotic;

import java.lang.invoke.MethodHandle;

/**
 * A TypeSwitch allows to encode a switch on types as a plain old switch on integers.
 * For that, a TypeSwitch is {@link #create(boolean, Class...) created} with an array of classes
 * and will try to find for an object the first class of this array which is a super-type of the object class.
 * 
 * The class inside the array must follow a partial order with the classes more specific (the subtypes) first
 * and classes less specific (the supertypes) after them.
 * 
 * An example of usage, instead of using a cascade of 'if instanceof'
 * <pre>
 * public static String asString(Object o) {
 *   if (o == null) {
 *     return "null";
 *   }
 *   if (o instanceof Integer) {
 *     return "Integer";
 *   }
 *   if (o instanceof String) {
 *     return "String";
 *   }
 *   // default
 *   return "unknown";
 * }
 * </pre>
 * 
 * a TypeSwitch allows to use a plain old switch to switch on type
 * <pre>
 * private static final TypeSwitch TYPE_SWITCH = TypeSwitch.create(true, Integer.class, String.class);
 * 
 * public static String asString(Object o) {
 *   switch(TYPE_SWITCH.typeSwitch(o)) {
 *   case TypeSwitch.NULL_MATCH:
 *     return "null";
 *   case 0:
 *     return "Integer";
 *   case 1:
 *     return "String";
 *   default: // TypeSwitch.BAD_MATCH
 *     return "unknown";
 *   }
 * }
 * </pre>
 *
 */
@FunctionalInterface
public interface TypeSwitch {
  /**
   * Returns the index of the first class in {@code typecases} that match with the class of {@code value} taken as parameter. 
   * @param value the value
   * @return the index of the first class that match the class of the {@code value},
   *   {@value #NULL_MATCH} if {@code value} is null or {@link #NO_MATCH} if no class in the array match the class. 
   * 
   * @see #create(boolean, Class...)
   */
  int typeSwitch(Object value);
  
  /**
   * Return value of {@link #typeSwitch(Object)} that indicates that no match is found.
   */
  int NO_MATCH = -2;
  
  /**
   * Return value of {@link #typeSwitch(Object)} that indicates that null is found.
   */
  int NULL_MATCH = -1;
  
  /**
   * Creates a TypeSwitch that returns for an object the index of its class/superclasses in the {@code typecases} array
   * or {@link #NO_MATCH} if no class match.
   * If several classes in {@code typecases} can match the class of the object, the first class in the array will be chosen.
   * 
   * @param nullMatch true is the TypeSwitch should allow null.
   * @param typecases an array 
   * @return a TypeSwitch configured with the array of typecases.
   * @throws NullPointerException is {@code typecases is null} or one element of the array is null.
   * 
   * @see TypeSwitch#typeSwitch(Object)
   */
  static TypeSwitch create(boolean nullMatch, Class<?>... typecases) {
    TypeSwitchCallSite.validatePartialOrder(typecases);
    MethodHandle mh = TypeSwitchCallSite.wrapNullIfNecessary(nullMatch, TypeSwitchCallSite.create(typecases).dynamicInvoker());
    return value -> {
      try {
        return (int)mh.invokeExact(value);  
      } catch(Throwable t) {
        throw Thrower.rethrow(t);
      }
    };
  }
}
