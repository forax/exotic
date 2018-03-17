package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class ConstantMemoizerExampleTests {
  private static final Function<Level, Integer> MEMOIZER =
      ConstantMemoizer.memoizer(Level::ordinal, Level.class, int.class);

  enum Level {
    LOW,
    HIGH
  }

  @Test
  void test() {
    assertEquals(0, (int) MEMOIZER.apply(Level.LOW));
    assertEquals(0, (int) MEMOIZER.apply(Level.LOW));
    assertEquals(1, (int) MEMOIZER.apply(Level.HIGH));
    assertEquals(1, (int) MEMOIZER.apply(Level.HIGH));
  }
}
