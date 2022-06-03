package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.constant;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MutableCallSite;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Create a constant value that can be changed from time to time. If the {@link #getter()} is stored
 * in a static final field, the result of the supplier is guaranteed to be seen as a constant by the
 * Virtual Machine.
 *
 * <p>To avoid unnecessary boxing in common cases of constant of type {@code int}, {@code long} and
 * {@code double}, there are specialized version of the {@link #getter()}, {@link #intGetter()},
 * {@link #longGetter()} and {@link #doubleGetter()}.
 *
 * <p>This work because when {@link #setAndDeoptimize(Object)} is called, all the assembly code
 * (JITed code) that where containing the constant are de-optimized and in the future they will be
 * re-optimized with the new value of the constant. So calling {@link #setAndDeoptimize(Object)} in
 * a loop will kill performance.
 *
 * <p>Example of usage
 *
 * <pre>
 *   private static final MostlyConstant&lt;Integer&gt; FOO = new MostlyConstant&lt;&gt;(42, int.class);
 *   private static final IntSupplier FOO_GETTER = FOO.intGetter();
 *
 *   public static int getFoo() {
 *     return FOO_GETTER.getAsInt();
 *   }
 *   public static void setFoo(int value) {
 *     FOO.setAndDeoptimize(value);
 *   }
 * </pre>
 *
 * @param <T> the type of the constant.
 */
public final class MostlyConstant<T> {
  private final Class<T> type;
  private final MutableCallSite callSite;
  private final MethodHandle invoker;

  /**
   * Create a constant with a value ({@code constant}) and its class ({@code type}).
   *
   * @param constant the value of the constant.
   * @param type the class of the constant.
   * @throws NullPointerException if type is null.
   * @throws ClassCastException if the constant cannot be converted to the type
   * @throws IllegalArgumentException is type is void.class
   */
  public MostlyConstant(T constant, Class<T> type) {
    this.type = Objects.requireNonNull(type);
    MethodHandle target = constant(type, constant);
    MutableCallSite callSite = new MutableCallSite(target.asType(target.type().erase()));
    this.callSite = callSite;
    this.invoker = callSite.dynamicInvoker();
  }

  /**
   * Change the value of the constant. This call requires the VM to de-optimize all the assembly
   * codes that contains the previous value of this constant, so this call will slow down the
   * application. Use this method with care, you have been warned.
   *
   * @param constant the new value of the constant.
   * @throws ClassCastException if the constant cannot be converted to constant type.
   */
  public void setAndDeoptimize(T constant) {
    MethodHandle target = constant(type, constant);
    callSite.setTarget(target.asType(callSite.type()));
    MutableCallSite.syncAll(new MutableCallSite[] { callSite });
  }

  /**
   * Returns a supplier that will return the value of this constant as a constant value. The
   * returned supplier should be stored in a static field for performance.
   *
   * @return a supplier that will return the value of this constant as a constant value.
   * @see MostlyConstant#intGetter()
   * @see MostlyConstant#longGetter()
   * @see MostlyConstant#doubleGetter()
   */
  public Supplier<T> getter() {
    MethodHandle invoker = this.invoker;
    return () -> {
      try {
        return (T) invoker.invokeExact();
      } catch (Throwable e) {
        throw Thrower.rethrow(e);
      }
    };
    /*return new Supplier<>() {
      @Override
      public T get() {
        try {
          return (T)invoker.invokeExact();
        } catch (Throwable e) {
          throw Thrower.rethrow(e);
        }
      }
    };*/
  }

  /**
   * Returns a supplier that will return the value of this constant as a constant value. The
   * returned supplier should be stored in a static field for performance.
   *
   * @return a supplier that will return the value of this constant as a constant value.
   * @throws IllegalStateException if the constant is not of type {@code int.class}.
   * @see MostlyConstant#getter()
   */
  public IntSupplier intGetter() {
    if (callSite.type().returnType() != int.class) {
      throw new IllegalStateException("the constant is not of type int.class");
    }
    MethodHandle invoker = this.invoker;
    return () -> {
      try {
        return (int) invoker.invokeExact();
      } catch (Throwable e) {
        throw Thrower.rethrow(e);
      }
    };
  }

  /**
   * Returns a supplier that will return the value of this constant as a constant value. The
   * returned supplier should be stored in a static field for performance.
   *
   * @return a supplier that will return the value of this constant as a constant value.
   * @throws IllegalStateException if the constant is not of type {@code long.class}.
   * @see MostlyConstant#getter()
   */
  public LongSupplier longGetter() {
    if (callSite.type().returnType() != long.class) {
      throw new IllegalStateException("the constant is not of type long.class");
    }
    MethodHandle invoker = this.invoker;
    return () -> {
      try {
        return (long) invoker.invokeExact();
      } catch (Throwable e) {
        throw Thrower.rethrow(e);
      }
    };
  }

  /**
   * Returns a supplier that will return the value of this constant as a constant value. The
   * returned supplier should be stored in a static field for performance.
   *
   * @return a supplier that will return the value of this constant as a constant value.
   * @throws IllegalStateException if the constant is not of type {@code double.class}.
   * @see MostlyConstant#getter()
   */
  public DoubleSupplier doubleGetter() {
    if (callSite.type().returnType() != double.class) {
      throw new IllegalStateException("the constant is not of type double.class");
    }
    MethodHandle invoker = this.invoker;
    return () -> {
      try {
        return (double) invoker.invokeExact();
      } catch (Throwable e) {
        throw Thrower.rethrow(e);
      }
    };
  }
}
