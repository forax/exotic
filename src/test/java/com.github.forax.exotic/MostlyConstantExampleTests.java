package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.IntSupplier;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class MostlyConstantExampleTests {
  private static final MostlyConstant<Integer> FOO = new MostlyConstant<>(42, int.class);
  private static final IntSupplier FOO_GETTER = FOO.intGetter();

  public static int getFoo() {
    return FOO_GETTER.getAsInt();
  }

  public static void setFoo(int value) {
    FOO.setAndDeoptimize(value);
  }

  @Test
  public void test() {
    assertEquals(42, getFoo());
    setFoo(43);
    assertEquals(43, getFoo());
  }
}
