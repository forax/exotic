package com.github.forax.exotic;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * Allow to call methods from different classes with no common interface
 * if they have the same name and the same parameter types.
 * 
 * In term of performance, those calls require boxing of arguments and return value
 * so performance may suffer in the case the VM is not able to eliminate the boxing
 * (sacrificing a goat may help).  
 * 
 * <pre>
 * private final static StructuralCall IS_EMPTY =
 *   StructuralCall.create(MethodHandles.lookup(), "isEmpty", MethodType.methodType(boolean.class));
 *
 * static boolean isEmpty(Object o) {
 *   return IS_EMPTY.invoke(o);
 * }
 * ...
 * 
 * System.out.println(isEmpty(List.of()));  // true
 * System.out.println(isEmpty(List.of(1))); // false
 * System.out.println(isEmpty(Set.of()));   // true
 * System.out.println(isEmpty(Map.of()));   // true
 * System.out.println(isEmpty(""));         // true
 * </pre>
 */
public interface StructuralCall {
  /**
   * Calls the structural method with no argument.
   * If the structural method is created with more parameters, this calls will add enough null arguments to match,
   * if the structural method is created with less parameters, this calls will drop enough arguments to match.
   * 
   * @param <R> type of the return type.
   * @param receiver the receiver of the call.
   * @return the return value of the call, null if the calls return void.
   * @throws NoSuchMethodError if the receiver and its super types doesn't declare a method with the same name
   *         and the same parameter types.
   * @throws IllegalAccessError if the receiver is not accessible from the lookup that was pass as parameter
   *         when creating this StructuralCall.
   */
  <R> R invoke(Object receiver);
  
  /**
   * Calls the structural method with one argument.
   * If the structural method is created with more parameters, this calls will add enough null arguments to match,
   * if the structural method is created with less parameters, this calls will drop enough arguments to match.
   * 
   * @param <R> type of the return type.
   * @param receiver the receiver of the call.
   * @param arg1 first argument.
   * @return the return value of the call, null if the calls return void.
   * @throws NoSuchMethodError if the receiver and its super types doesn't declare a method with the same name
   *         and the same parameter types.
   * @throws IllegalAccessError if the receiver is not accessible from the lookup that was pass as parameter
   *         when creating this StructuralCall.
   */
  <R> R invoke(Object receiver, Object arg1);
  
  /**
   * Calls the structural method with two arguments.
   * If the structural method is created with more parameters, this calls will add enough null arguments to match,
   * if the structural method is created with less parameters, this calls will drop enough arguments to match.
   * 
   * @param <R> type of the return type.
   * @param receiver the receiver of the call.
   * @param arg1 first argument.
   * @param arg2 second argument.
   * @return the return value of the call, null if the calls return void.
   * @throws NoSuchMethodError if the receiver and its super types doesn't declare a method with the same name
   *         and the same parameter types.
   * @throws IllegalAccessError if the receiver is not accessible from the lookup that was pass as parameter
   *         when creating this StructuralCall.
   */
  <R> R invoke(Object receiver, Object arg1, Object arg2);
  
  /**
   * Calls the structural method with three arguments.
   * If the structural method is created with more parameters, this calls will add enough null arguments to match,
   * if the structural method is created with less parameters, this calls will drop enough arguments to match.
   * 
   * @param <R> type of the return type.
   * @param receiver the receiver of the call.
   * @param arg1 first argument.
   * @param arg2 second argument.
   * @param arg3 third argument.
   * @return the return value of the call, null if the calls return void.
   * @throws NoSuchMethodError if the receiver and its super types doesn't declare a method with the same name
   *         and the same parameter types.
   * @throws IllegalAccessError if the receiver is not accessible from the lookup that was pass as parameter
   *         when creating this StructuralCall.
   */
  <R> R invoke(Object receiver, Object arg1, Object arg2, Object arg3);
  
  /**
   * Calls the structural method with four arguments.
   * If the structural method is created with more parameters, this calls will add enough null arguments to match,
   * if the structural method is created with less parameters, this calls will drop enough arguments to match.
   * 
   * @param <R> type of the return type.
   * @param receiver the receiver of the call.
   * @param arg1 first argument.
   * @param arg2 second argument.
   * @param arg3 third argument.
   * @param arg4 fourth argument.
   * @return the return value of the call, null if the calls return void.
   * @throws NoSuchMethodError if the receiver and its super types doesn't declare a method with the same name
   *         and the same parameter types.
   * @throws IllegalAccessError if the receiver is not accessible from the lookup that was pass as parameter
   *         when creating this StructuralCall.
   */
  <R> R invoke(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4);
  
  /**
   * Calls the structural method with five arguments.
   * If the structural method is created with more parameters, this calls will add enough null arguments to match,
   * if the structural method is created with less parameters, this calls will drop enough arguments to match.
   * 
   * @param <R> type of the return type.
   * @param receiver the receiver of the call.
   * @param arg1 first argument.
   * @param arg2 second argument.
   * @param arg3 third argument.
   * @param arg4 fourth argument.
   * @param arg5 fifth argument.
   * @return the return value of the call, null if the calls return void.
   * @throws NoSuchMethodError if the receiver and its super types doesn't declare a method with the same name
   *         and the same parameter types.
   * @throws IllegalAccessError if the receiver is not accessible from the lookup that was pass as parameter
   *         when creating this StructuralCall.
   */
  <R> R invoke(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5);
  
  /**
   * Calls the structural method with six arguments.
   * If the structural method is created with more parameters, this calls will add enough null arguments to match,
   * if the structural method is created with less parameters, this calls will drop enough arguments to match.
   * 
   * @param <R> type of the return type.
   * @param receiver the receiver of the call.
   * @param arg1 first argument.
   * @param arg2 second argument.
   * @param arg3 third argument.
   * @param arg4 fourth argument.
   * @param arg5 fifth argument.
   * @param arg6 sixth argument.
   * @return the return value of the call, null if the calls return void.
   * @throws NoSuchMethodError if the receiver and its super types doesn't declare a method with the same name
   *         and the same parameter types.
   * @throws IllegalAccessError if the receiver is not accessible from the lookup that was pass as parameter
   *         when creating this StructuralCall.
   */
  <R> R invoke(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6);
  
  /**
   * Calls the structural method with seven arguments.
   * If the structural method is created with more parameters, this calls will add enough null arguments to match,
   * if the structural method is created with less parameters, this calls will drop enough arguments to match.
   * 
   * @param <R> type of the return type.
   * @param receiver the receiver of the call.
   * @param arg1 first argument.
   * @param arg2 second argument.
   * @param arg3 third argument.
   * @param arg4 fourth argument.
   * @param arg5 fifth argument.
   * @param arg6 sixth argument.
   * @param arg7 seventh argument.
   * @return the return value of the call, null if the calls return void.
   * @throws NoSuchMethodError if the receiver and its super types doesn't declare a method with the same name
   *         and the same parameter types.
   * @throws IllegalAccessError if the receiver is not accessible from the lookup that was pass as parameter
   *         when creating this StructuralCall.
   */
  <R> R invoke(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7);
  
  /**
   * Calls the structural method with eight arguments.
   * If the structural method is created with more parameters, this calls will add enough null arguments to match,
   * if the structural method is created with less parameters, this calls will drop enough arguments to match.
   * 
   * @param <R> type of the return type.
   * @param receiver the receiver of the call.
   * @param arg1 first argument.
   * @param arg2 second argument.
   * @param arg3 third argument.
   * @param arg4 fourth argument.
   * @param arg5 fifth argument.
   * @param arg6 sixth argument.
   * @param arg7 seventh argument.
   * @param arg8 eighth argument.
   * @return the return value of the call, null if the calls return void.
   * @throws NoSuchMethodError if the receiver and its super types doesn't declare a method with the same name
   *         and the same parameter types.
   * @throws IllegalAccessError if the receiver is not accessible from the lookup that was pass as parameter
   *         when creating this StructuralCall.
   */
  <R> R invoke(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8);
  
  /**
   * Creates a structural call that can call methods visible from the {@code lookup},
   * with name {@code name} and parameter types {@code type}.
   * 
   * @param lookup will be used to find the methods in the receiver classes.
   * @param name the name of the methods.
   * @param type the parameter types of the methods.
   * @return a new structural call that will call methods structurally.
   */
  static StructuralCall create(Lookup lookup, String name, MethodType type) {
    MethodHandle mh = StructuralCallImpl.findMethodHandle(lookup, name, type);
    return (StructuralCallImpl)(receiver, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8) -> {
      try {
        return mh.invokeExact(receiver, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
      } catch (Throwable e) {
        throw rethrow(e);
      }
    };
  }
  
  private static UndeclaredThrowableException rethrow(Throwable t) {
    if (t instanceof RuntimeException) {
      throw (RuntimeException)t;
    }
    if (t instanceof Error) {
      throw (Error)t;
    }
    return new UndeclaredThrowableException(t);
  }
}
