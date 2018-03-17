package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.exactInvoker;
import static java.lang.invoke.MethodHandles.foldArguments;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.util.Objects;
import java.util.function.Function;

/**
 * Allow to see the return value of a function as a constant in case there are few possible pairs of
 * key/value.
 *
 * <p>Here is an example of usage
 *
 * <pre>
 *  enum Level {
 *    LOW, HIGH
 *  }
 *  ...
 *  private static final Function&lt;Level, Integer&gt; MEMOIZER = ConstantMemoizer.memoizer(Level::ordinal, Level.class, int.class);
 *  ...
 *  int result = MEMOIZER.apply(Level.LOW));  // if this line is called several times,
 *                                            // the result will be considered as constant.
 *  </pre>
 */
public final class ConstantMemoizer {
  private ConstantMemoizer() {
    throw new AssertionError();
  }

  /**
   * Return a function that returns a constant value (for the Virtual Machine) for each key taken as
   * argument. The value corresponding to a key is calculated by calling the {@code function} once
   * by key and then cached in a code similar to a cascade of {@code if equals else}.
   *
   * <p>To find if a key was previously seen or not, {@link Object#equals(Object)} will be called to
   * compare the actual key with possibly all the keys already seen, so if there are a lot of
   * different keys, the performance in the worst case is like a linear search i.e. O(number of seen
   * keys).
   *
   * @param <K> type of the keys.
   * @param <V> type of the values.
   * @param function a function that takes a non null key as argument and return a non null value.
   * @param keyClass the class of the key, if it's a primitive type, the key value will be boxed
   *     before calling the {@code function}.
   * @param valueClass the class of the value, if it's a primitive type, the value will be boxed at
   *     each call.
   * @return a function the function getting the value for a specific key.
   * @throws NullPointerException if the {@code function}, the {@code keyClass} or the {@code
   *     valueClass} is null, or if the function key or the function value is null.
   * @throws ClassCastException if the function key or the function value types doesn't match the
   *     {@code keyClass} or the {@code valueClass}.
   */
  public static <K, V> Function<K, V> memoizer(
      Function<? super K, ? extends V> function, Class<K> keyClass, Class<V> valueClass) {
    Objects.requireNonNull(function);
    Objects.requireNonNull(keyClass);
    Objects.requireNonNull(valueClass);
    MethodHandle mh =
        new InliningCacheCallSite<>(methodType(valueClass, keyClass), function)
            .dynamicInvoker()
            .asType(methodType(Object.class, Object.class)); // erase
    return key -> {
      Objects.requireNonNull(key);
      try {
        return (V) mh.invokeExact(key);
      } catch (Throwable e) {
        throw Thrower.rethrow(e);
      }
    };
  }

  private static class InliningCacheCallSite<K, V> extends MutableCallSite {
    private static final MethodHandle FALLBACK, EQUALS;

    static {
      Lookup lookup = lookup();
      try {
        FALLBACK =
            lookup.findVirtual(
                InliningCacheCallSite.class,
                "fallback",
                methodType(MethodHandle.class, Object.class));
        EQUALS =
            lookup.findVirtual(Object.class, "equals", methodType(boolean.class, Object.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }

    private final Function<? super K, ? extends V> function;

    InliningCacheCallSite(MethodType type, Function<? super K, ? extends V> function) {
      super(type);
      this.function = function;
      setTarget(
          foldArguments(
              exactInvoker(type),
              FALLBACK.bindTo(this).asType(methodType(MethodHandle.class, type.parameterType(0)))));
    }

    @SuppressWarnings("unused")
    private MethodHandle fallback(K key) {
      V value = Objects.requireNonNull(function.apply(key));
      MethodType type = type();
      Class<?> keyClass = type.parameterType(0);
      Class<?> valueClass = type.returnType();
      MethodHandle target = dropArguments(constant(valueClass, value), 0, keyClass);
      setTarget(
          guardWithTest(
              EQUALS.bindTo(key).asType(methodType(boolean.class, keyClass)),
              target,
              new InliningCacheCallSite<>(type, function).dynamicInvoker()));
      return target;
    }
  }
}
