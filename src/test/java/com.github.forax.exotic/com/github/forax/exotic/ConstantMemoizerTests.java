package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class ConstantMemoizerTests {
  private static final Function<Integer, Integer> FIBO =
      ConstantMemoizer.memoizer(n -> fibo(n), int.class, int.class);

  private static int fibo(int n) {
    if (n < 2) {
      return 1;
    }
    return FIBO.apply(n - 2) + FIBO.apply(n - 1);
  }

  @Test
  void testRecursive() {
    assertEquals(21, (int) FIBO.apply(7));
  }

  @Test
  void testSimple() {
    Function<String, Integer> parseInt =
        ConstantMemoizer.memoizer(Integer::parseInt, String.class, int.class);
    assertEquals(666, (int) parseInt.apply("666"));
    assertEquals(666, (int) parseInt.apply("666"));
  }

  @Test
  void testArgumentNull() {
    Function<Integer, Integer> fun =
        ConstantMemoizer.memoizer(x -> x, Integer.class, Integer.class);
    assertThrows(NullPointerException.class, () -> fun.apply(null));
  }

  @Test
  void testReturnValueNull() {
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
  void testWrongParameterType() {
    @SuppressWarnings("unchecked")
    Function<Integer, String> fun =
        ConstantMemoizer.memoizer(
            (Function<Integer, String>) (Function<?, ?>) (String x) -> x,
            Integer.class,
            String.class);
    assertThrows(ClassCastException.class, () -> fun.apply(666));
  }
}
