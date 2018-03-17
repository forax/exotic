package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.ToIntFunction;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class ConstantMemoizerExampleTests {
  private static final ToIntFunction<Level> MEMOIZER =
      ConstantMemoizer.intMemoizer(Level::ordinal, Level.class);

  enum Level {
    LOW,
    HIGH
  }

  @Test
  void test() {
    assertEquals(0, MEMOIZER.applyAsInt(Level.LOW));
    assertEquals(0, MEMOIZER.applyAsInt(Level.LOW));
    assertEquals(1, MEMOIZER.applyAsInt(Level.HIGH));
    assertEquals(1, MEMOIZER.applyAsInt(Level.HIGH));
  }
}
