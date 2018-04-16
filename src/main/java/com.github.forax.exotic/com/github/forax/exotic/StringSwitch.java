package com.github.forax.exotic;

import java.lang.invoke.MethodHandle;

/**
 * A StringSwitch allows to encode a switch on strings as a plain old switch on integers.
 * For that, a StringSwitch is {@link #create(boolean, String...) created} with an array of strings
 * and will try to find for a string the index of the same string in the array.
 * 
 * An example of usage, instead of using a cascade of 'if equals'
 * <pre>
 * public static String owner(String s) {
 *   if (o == null) {
 *     return "not owner";
 *   }
 *   if (s.equals("bernie the dog") {
 *     return "john";
 *   }
 *   if (s.equals("zara the cat") {
 *     return "jane";
 *   }
 *   // default
 *   return "unknown owner";
 * }
 * </pre>
 * 
 * a StringSwitch allows to use a plain old switch to switch on string
 * <pre>
 * private static final StringSwitch STRING_SWITCH = StringSwitch.create(true, "bernie the dog", "zara the cat");
 *
 * public static String owner(String s) {
 *   switch(STRING_SWITCH.stringSwitch(s)) {
 *   case StringSwitch.NULL_MATCH:
 *     return "no owner";
 *   case 0:
 *     return "john";
 *   case 1:
 *     return "jane";
 *   default: // TypeSwitch.BAD_MATCH
 *     return "unknown owner";
 *   }
 * }
 * </pre>
 *
 */
@FunctionalInterface
public interface StringSwitch {
  /**
   * Returns the index of the first class in {@code stringcases} that match with the class of {@code value} taken as parameter. 
   * @param value the value
   * @return the index of the first class that match the class of the {@code value},
   *   {@value #NULL_MATCH} if {@code value} is null or {@link #NO_MATCH} if no class in the array match the class. 
   * 
   * @see #create(boolean, String...)
   */
  int stringSwitch(String value);
  
  /**
   * Return value of {@link #stringSwitch(String)} that indicates that no match is found.
   */
  int NO_MATCH = -2;
  
  /**
   * Return value of {@link #stringSwitch(String)} that indicates that null is found.
   */
  int NULL_MATCH = -1;
  
  /**
   * Creates a StringSwitch that returns for a string the index in the {@code stringcases} array
   * or {@link #NO_MATCH} if no string match.
   * 
   * @param nullMatch true is the StringSwitch should allow null.
   * @param stringcases an array of string. 
   * @return a StringSwitch configured with the array of stringcases.
   * @throws NullPointerException is {@code stringcases is null} or one string of the array is null.
   * @throws IllegalStateException if the same string appears several times in the array.
   * 
   * @see StringSwitch#stringSwitch(String)
   */
  static StringSwitch create(boolean nullMatch, String... stringcases) {
    MethodHandle mh = StringSwitchCallSite.wrapNullIfNecessary(nullMatch, StringSwitchCallSite.create(stringcases).dynamicInvoker());
    return value -> {
      try {
        return (int)mh.invokeExact(value);  
      } catch(Throwable t) {
        throw Thrower.rethrow(t);
      }
    };
  }
}
