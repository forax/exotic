package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.exactInvoker;
import static java.lang.invoke.MethodHandles.foldArguments;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MutableCallSite;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * A utility class that allow to create getters of class fields with a stable semantics.
 * 
 * The method {@link #getter(Lookup, Class, String, Class)} returns a general purpose getter
 * while the methods {@link #intGetter(Lookup, Class, String)},
 * {@link #longGetter(Lookup, Class, String)} and
 * {@link #doubleGetter(Lookup, Class, String)} returns getters specialized
 * if the type of the field is an int, a long or a double (respectively).
 * 
 * Here is an example of a kind of lazy initialization of a field of a singleton object using a stable value.
 * The returned value of {@code getCpuCount()} is a constant once initialized. 
 * <pre>
 * class SystemInfo {
 *   static final ToIntFunction&lt;SystemInfo&gt; CPU_COUNT = StableField.intGetter(lookup(), SystemInfo.class, "cpuCount");
 *   static final SystemInfo INSTANCE = new SystemInfo();
 *   
 *   private SystemInfo() {
 *     // enforce singleton
 *   }
 *   
 *   volatile int cpuCount;  // stable
 *
 *   public int getCpuCount() {
 *     int cpuCount = CPU_COUNT.applyAsInt(this);
 *     if (cpuCount == 0) {
 *       return this.cpuCount = Runtime.getRuntime().availableProcessors();
 *     }
 *     return cpuCount;
 *   }
 * }
 * </pre>
 * 
 * The stable semantics is defined by the following rules:
 * If the field is not initialized or initialized with its default value,
 * the default value will be returned when calling the getter.
 * 
 * If the field is initialized with another value than the default value,
 * the getter will return the first value of the field observed by the getter,
 * any subsequent calls to the getter will return this same value.
 * 
 * If the getter has observed a value different from the default value,
 * any subsequent calls to the getter need to pass the same object as argument
 * of the getter.
 */
public final class StableField {
  private StableField() {
    throw new AssertionError();
  }
  
  /**
   * Create a getter on a field of a class with a stable semantics.
   * If the type of the field is a primitive type, the value will be boxed.
   * 
   * If the field is not initialized or initialized with its default value,
   * the default value will be returned when calling the getter.
   * If the field is initialized with another value than the default value,
   * the getter will return the first value of the field observed by the getter,
   * any subsequent calls to the getter will return this same value.
   * 
   * If the getter has observed a value different from the default value,
   * any subsequent calls to the getter need to pass the same object as argument
   * of the getter.
   * 
   * @param <T> the type of the object containing the field.
   * @param <V> the type of the field.
   * @param lookup a lookup object that can access to the field.
   * @param declaringClass the class that declares the field.
   * @param name the name of the field.
   * @param type the type of the field.
   * @return a function that takes an object of the {@code declaring class} and returns the value of the field.
   * @throws NullPointerException if either the lookup, the declaring class, the name or the type is null.
   * @throws NoSuchFieldError if the field doesn't exist. 
   * @throws IllegalAccessError if the field is not accessible from the lookup.
   * @throws IllegalStateException if the argument of the getter is not constant.
   */
  public static <T, V> Function<T, V> getter(Lookup lookup, Class<T> declaringClass, String name, Class<V> type) {
    Objects.requireNonNull(lookup);
    Objects.requireNonNull(declaringClass);
    Objects.requireNonNull(name);
    Objects.requireNonNull(type);
    MethodHandle getter = createGetter(lookup, declaringClass, name, type);
    MethodHandle mh = new StableFieldCS(getter, Object.class).dynamicInvoker();
    return object -> {
      try {
        return (V)mh.invokeExact(object);
      } catch(Throwable t) {
        throw rethrow(t);
      }
    };
  }
  
  /**
   * Create a getter on a field of type {@code int} of a class with a stable semantics.
   * 
   * If the field is not initialized or initialized with its default value,
   * the default value will be returned when calling the getter.
   * If the field is initialized with another value than the default value,
   * the getter will return the first value of the field observed by the getter,
   * any subsequent calls to the getter will return this same value.
   * 
   * If the getter has observed a value different from the default value,
   * any subsequent calls to the getter need to pass the same object as argument
   * of the getter.
   * 
   * This call is equivalent to a call to {@link #getter(Lookup, Class, String, Class)} with
   * {@code int.class} as last argument that returns a getter that doesn't box the return value. 
   * 
   * @param <T> the type of the object containing the field.
   * @param lookup a lookup object that can access to the field.
   * @param declaringClass the class that declares the field.
   * @param name the name of the field.
   * @return a function that takes an object of the {@code declaring class} and returns the value of the field.
   * @throws NullPointerException if either the lookup, the declaring class or the name is null.
   * @throws NoSuchFieldError if the field doesn't exist. 
   * @throws IllegalAccessError if the field is not accessible from the lookup.
   * @throws IllegalStateException if the argument of the getter is not constant.
   */
  public static <T> ToIntFunction<T> intGetter(Lookup lookup, Class<T> declaringClass, String name) {
    Objects.requireNonNull(lookup);
    Objects.requireNonNull(declaringClass);
    Objects.requireNonNull(name);
    MethodHandle getter = createGetter(lookup, declaringClass, name, int.class);
    MethodHandle mh = new StableFieldCS(getter, int.class).dynamicInvoker();
    return object -> {
      try {
        return (int)mh.invokeExact(object);
      } catch(Throwable t) {
        throw rethrow(t);
      }
    };
  }
  
  /**
   * Create a getter on a field of type {@code long} of a class with a stable semantics.
   * 
   * If the field is not initialized or initialized with its default value,
   * the default value will be returned when calling the getter.
   * If the field is initialized with another value than the default value,
   * the getter will return the first value of the field observed by the getter,
   * any subsequent calls to the getter will return this same value.
   * 
   * If the getter has observed a value different from the default value,
   * any subsequent calls to the getter need to pass the same object as argument
   * of the getter.
   * 
   * This call is equivalent to a call to {@link #getter(Lookup, Class, String, Class)} with
   * {@code long.class} as last argument that returns a getter that doesn't box the return value. 
   * 
   * @param <T> the type of the object containing the field.
   * @param lookup a lookup object that can access to the field.
   * @param declaringClass the class that declares the field.
   * @param name the name of the field.
   * @return a function that takes an object of the {@code declaring class} and returns the value of the field.
   * @throws NullPointerException if either the lookup, the declaring class or the name is null.
   * @throws NoSuchFieldError if the field doesn't exist. 
   * @throws IllegalAccessError if the field is not accessible from the lookup.
   * @throws IllegalStateException if the argument of the getter is not constant.
   */
  public static <T> ToLongFunction<T> longGetter(Lookup lookup, Class<T> declaringClass, String name) {
    Objects.requireNonNull(lookup);
    Objects.requireNonNull(declaringClass);
    Objects.requireNonNull(name);
    MethodHandle getter = createGetter(lookup, declaringClass, name, long.class);
    MethodHandle mh = new StableFieldCS(getter, long.class).dynamicInvoker();
    return object -> {
      try {
        return (long)mh.invokeExact(object);
      } catch(Throwable t) {
        throw rethrow(t);
      }
    };
  }
  
  /**
   * Create a getter on a field of type {@code double} of a class with a stable semantics.
   * 
   * If the field is not initialized or initialized with its default value,
   * the default value will be returned when calling the getter.
   * If the field is initialized with another value than the default value,
   * the getter will return the first value of the field observed by the getter,
   * any subsequent calls to the getter will return this same value.
   * 
   * If the getter has observed a value different from the default value,
   * any subsequent calls to the getter need to pass the same object as argument
   * of the getter.
   * 
   * This call is equivalent to a call to {@link #getter(Lookup, Class, String, Class)} with
   * {@code double.class} as last argument that returns a getter that doesn't box the return value. 
   * 
   * @param <T> the type of the object containing the field.
   * @param lookup a lookup object that can access to the field.
   * @param declaringClass the class that declares the field.
   * @param name the name of the field.
   * @return a function that takes an object of the {@code declaring class} and returns the value of the field.
   * @throws NullPointerException if either the lookup, the declaring class or the name is null.
   * @throws NoSuchFieldError if the field doesn't exist. 
   * @throws IllegalAccessError if the field is not accessible from the lookup.
   * @throws IllegalStateException if the argument of the getter is not constant.
   */
  public static <T> ToDoubleFunction<T> doubleGetter(Lookup lookup, Class<T> declaringClass, String name) {
    Objects.requireNonNull(lookup);
    Objects.requireNonNull(declaringClass);
    Objects.requireNonNull(name);
    MethodHandle getter = createGetter(lookup, declaringClass, name, double.class);
    MethodHandle mh = new StableFieldCS(getter, double.class).dynamicInvoker();
    return object -> {
      try {
        return (double)mh.invokeExact(object);
      } catch(Throwable t) {
        throw rethrow(t);
      }
    };
  }
  
  private static <T, V> MethodHandle createGetter(Lookup lookup, Class<T> declaringClass, String name, Class<V> type)
      throws NoSuchFieldError, IllegalAccessError {
    try {
      return lookup.findGetter(declaringClass, name, type);
    } catch (NoSuchFieldException e) {
      throw (NoSuchFieldError)new NoSuchFieldError().initCause(e);
    } catch(IllegalAccessException e) {
      throw (IllegalAccessError)new IllegalAccessError().initCause(e);
    }
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
  
  private static class StableFieldCS extends MutableCallSite {
    private static final MethodHandle FALLBACK, VALUE_CHECK, NOT_CONSTANT;
    static {
      Lookup lookup = MethodHandles.lookup();
      try {
        FALLBACK = lookup.findVirtual(StableFieldCS.class, "fallback", methodType(MethodHandle.class, Object.class));
        VALUE_CHECK = lookup.findStatic(StableFieldCS.class, "valueCheck", methodType(boolean.class, Object.class, Object.class));
        NOT_CONSTANT = lookup.findStatic(StableFieldCS.class, "notConstant", methodType(void.class, Object.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
    
    private final MethodHandle getter;
    
    StableFieldCS(MethodHandle getter, Class<?> returnType) {
      super(methodType(returnType, Object.class));
      this.getter = getter;
      setTarget(foldArguments(exactInvoker(type()), FALLBACK.bindTo(this)));
    }
    
    @SuppressWarnings("unused")
    private MethodHandle fallback(Object o) throws Throwable {
      Objects.requireNonNull(o);
      Object result = getter.invoke(o);
      MethodHandle constant = dropArguments(constant(getter.type().returnType(), result), 0, Object.class).asType(type());
      if (!Objects.equals(result, zero(getter.type().returnType()))) {
        MethodHandle target = MethodHandles.guardWithTest(VALUE_CHECK.bindTo(o), constant, NOT_CONSTANT.asType(type()));
        setTarget(target);  
      }
      return constant;
    }
    
    private static Object zero(Class<?> type) {
      if (type == int.class) {
        return 0;
      }
      if (type == long.class) {
        return 0L;
      }
      if (type == double.class) {
        return 0.0;
      }
      if (type == boolean.class) {
        return false;
      }
      if (type == byte.class) {
        return (byte)0;
      }
      if (type == short.class) {
        return (short)0;
      }
      if (type == char.class) {
        return (char)0;
      }
      if (type == float.class) {
        return 0f;
      }
      return null;
    }
    
    @SuppressWarnings("unused")
    private static boolean valueCheck(Object v1, Object v2) {
      return v1 == v2;
    }
    
    @SuppressWarnings("unused")
    private static void notConstant(Object o) {
      throw new IllegalStateException("the receiver is not constant");
    }
  }
}
