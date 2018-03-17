package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;
import java.util.function.ToIntFunction;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class ConstantMemoizerTests {
  private static final Function<Integer, Integer> OBJECT_FIBO =
      ConstantMemoizer.memoizer(n -> objectFibo(n), int.class, int.class);

  private static int objectFibo(int n) {
    if (n < 2) {
      return 1;
    }
    return OBJECT_FIBO.apply(n - 2) + OBJECT_FIBO.apply(n - 1);
  }

  @Test
  void testObjectRecursive() {
    assertEquals(21, (int) OBJECT_FIBO.apply(7));
  }

  private static final ToIntFunction<Integer> INT_FIBO =
      ConstantMemoizer.intMemoizer(n -> intFibo(n), int.class);

  private static int intFibo(int n) {
    if (n < 2) {
      return 1;
    }
    return INT_FIBO.applyAsInt(n - 2) + INT_FIBO.applyAsInt(n - 1);
  }

  @Test
  void testIntRecursive() {
    assertEquals(21, INT_FIBO.applyAsInt(7));
  }

  @Test
  void testObjectSimple() {
    Function<String, Integer> parseInt =
        ConstantMemoizer.memoizer(Integer::parseInt, String.class, int.class);
    assertEquals(666, (int) parseInt.apply("666"));
    assertEquals(666, (int) parseInt.apply("666"));
  }

  @Test
  void testIntSimple() {
    ToIntFunction<String> parseInt = ConstantMemoizer.intMemoizer(Integer::parseInt, String.class);
    assertEquals(777, parseInt.applyAsInt("777"));
    assertEquals(777, parseInt.applyAsInt("777"));
  }

  @Test
  void testObjectArgumentNull() {
    Function<Integer, Integer> fun =
        ConstantMemoizer.memoizer(x -> x, Integer.class, Integer.class);
    assertThrows(NullPointerException.class, () -> fun.apply(null));
  }

  @Test
  void testIntArgumentNull() {
    ToIntFunction<Integer> fun = ConstantMemoizer.intMemoizer(x -> x, Integer.class);
    assertThrows(NullPointerException.class, () -> fun.applyAsInt(null));
  }

  @Test
  void testObjectReturnValueNull() {
    Function<Integer, Integer> fun =
        ConstantMemoizer.memoizer(x -> null, Integer.class, Integer.class);
    assertThrows(NullPointerException.class, () -> fun.apply(3));
  }

  @Test
  void testWrongReturnType() {
    @SuppressWarnings("unchecked")
    Function<String, Integer> fun =
        ConstantMemoizer.memoizer(
            (Function<String, Integer>) (Function<?, ?>) x -> x, String.class, Integer.class);
    assertThrows(ClassCastException.class, () -> fun.apply("boom !"));
  }

  @Test
  void testObjectWrongParameterType() {
    @SuppressWarnings("unchecked")
    Function<Integer, String> fun =
        ConstantMemoizer.memoizer(
            (Function<Integer, String>) (Function<?, ?>) (String x) -> x,
            Integer.class,
            String.class);
    assertThrows(ClassCastException.class, () -> fun.apply(666));
  }

  @Test
  void testIntWrongParameterType() {
    @SuppressWarnings("unchecked")
    ToIntFunction<Integer> fun =
        ConstantMemoizer.intMemoizer(
            (ToIntFunction<Integer>) (ToIntFunction<?>) (String x) -> Integer.parseInt(x),
            Integer.class);
    assertThrows(ClassCastException.class, () -> fun.applyAsInt(666));
  }
}
