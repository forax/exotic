package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class MostlyConstantTests {
  static class ObjectSandbox1 {
    static final MostlyConstant<String> VALUE = new MostlyConstant<>("hello", String.class);
    static final Supplier<String> VALUE_GETTER = VALUE.getter();
  }

  @Test
  void testObjectSimpleChange() {
    assertEquals("hello", ObjectSandbox1.VALUE_GETTER.get());
    ObjectSandbox1.VALUE.setAndDeoptimize("hell");
    assertEquals("hell", ObjectSandbox1.VALUE_GETTER.get());
  }

  static class ObjectSandbox2 {
    static final MostlyConstant<String> VALUE = new MostlyConstant<>("hello", String.class);
    static final Supplier<String> VALUE_GETTER = VALUE.getter();
  }

  @Test
  void testObjectSimpleChangeOptimized() {
    class Fake {
      String test() {
        return ObjectSandbox2.VALUE_GETTER.get();
      }
    }

    Fake fake = new Fake();
    for (int i = 0; i < 1_000_000; i++) {
      assertEquals("hello", fake.test());
    }
    assertEquals("hello", fake.test());

    ObjectSandbox2.VALUE.setAndDeoptimize("hell");
    assertEquals("hell", fake.test());
  }

  static class IntSandbox1 {
    static final MostlyConstant<Integer> VALUE = new MostlyConstant<>(42, int.class);
    static final IntSupplier VALUE_GETTER = VALUE.intGetter();
  }

  @Test
  void testIntSimpleChange() {
    assertEquals(42, IntSandbox1.VALUE_GETTER.getAsInt());
    IntSandbox1.VALUE.setAndDeoptimize(43);
    assertEquals(43, IntSandbox1.VALUE_GETTER.getAsInt());
  }

  static class IntSandbox2 {
    static final MostlyConstant<Integer> VALUE = new MostlyConstant<>(42, int.class);
    static final IntSupplier VALUE_GETTER = VALUE.intGetter();
  }

  @Test
  void testIntSimpleChangeOptimized() {
    class Fake {
      int test() {
        return IntSandbox2.VALUE_GETTER.getAsInt();
      }
    }

    Fake fake = new Fake();
    for (int i = 0; i < 1_000_000; i++) {
      assertEquals(42, fake.test());
    }
    assertEquals(42, fake.test());

    IntSandbox2.VALUE.setAndDeoptimize(43);
    assertEquals(43, fake.test());
  }

  static class LongSandbox1 {
    static final MostlyConstant<Long> VALUE = new MostlyConstant<>(42L, long.class);
    static final LongSupplier VALUE_GETTER = VALUE.longGetter();
  }

  @Test
  void testLongSimpleChange() {
    assertEquals(42, LongSandbox1.VALUE_GETTER.getAsLong());
    LongSandbox1.VALUE.setAndDeoptimize(43L);
    assertEquals(43, LongSandbox1.VALUE_GETTER.getAsLong());
  }

  static class LongSandbox2 {
    static final MostlyConstant<Long> VALUE = new MostlyConstant<>(42L, long.class);
    static final LongSupplier VALUE_GETTER = VALUE.longGetter();
  }

  @Test
  void testLongSimpleChangeOptimized() {
    class Fake {
      long test() {
        return LongSandbox2.VALUE_GETTER.getAsLong();
      }
    }

    Fake fake = new Fake();
    for (int i = 0; i < 1_000_000; i++) {
      assertEquals(42L, fake.test());
    }
    assertEquals(42L, fake.test());

    LongSandbox2.VALUE.setAndDeoptimize(43L);
    assertEquals(43L, fake.test());
  }

  static class DoubleSandbox1 {
    static final MostlyConstant<Double> VALUE = new MostlyConstant<>(42.0, double.class);
    static final DoubleSupplier VALUE_GETTER = VALUE.doubleGetter();
  }

  @Test
  void testDoubleSimpleChange() {
    assertEquals(42.0, DoubleSandbox1.VALUE_GETTER.getAsDouble());
    DoubleSandbox1.VALUE.setAndDeoptimize(43.0);
    assertEquals(43, DoubleSandbox1.VALUE_GETTER.getAsDouble());
  }

  static class DoubleSandbox2 {
    static final MostlyConstant<Double> VALUE = new MostlyConstant<>(42.0, double.class);
    static final DoubleSupplier VALUE_GETTER = VALUE.doubleGetter();
  }

  @Test
  void testDoubleSimpleChangeOptimized() {
    class Fake {
      double test() {
        return DoubleSandbox2.VALUE_GETTER.getAsDouble();
      }
    }

    Fake fake = new Fake();
    for (int i = 0; i < 1_000_000; i++) {
      assertEquals(42L, fake.test());
    }
    assertEquals(42L, fake.test());

    DoubleSandbox2.VALUE.setAndDeoptimize(43.0);
    assertEquals(43L, fake.test());
  }

  @Test
  void testConstructorWithVoidType() {
    assertThrows(IllegalArgumentException.class, () -> new MostlyConstant<>(null, void.class));
  }

  @Test
  void testConstructorWithNullType() {
    assertThrows(NullPointerException.class, () -> new MostlyConstant<>(null, null));
  }

  @Test
  void testSpecializedGettersWithWrapperTypes() {
    assertThrows(
        IllegalStateException.class, () -> new MostlyConstant<>(0, Integer.class).intGetter());
    assertThrows(
        IllegalStateException.class, () -> new MostlyConstant<>(0L, Long.class).longGetter());
    assertThrows(
        IllegalStateException.class, () -> new MostlyConstant<>(0.0, Double.class).doubleGetter());

    assertThrows(
        IllegalStateException.class, () -> new MostlyConstant<>(0, Object.class).intGetter());
    assertThrows(
        IllegalStateException.class, () -> new MostlyConstant<>(0L, Object.class).longGetter());
    assertThrows(
        IllegalStateException.class, () -> new MostlyConstant<>(0.0, Object.class).doubleGetter());
  }
}
