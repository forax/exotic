package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.publicLookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class StableFieldTests {
  static class A {
    String x;
  }

  @Test
  public void testObjectFieldUninitialized() {
    Function<A, String> xField = StableField.getter(lookup(), A.class, "x", String.class);
    A a = new A();
    assertNull(xField.apply(a));
    assertNull(xField.apply(a));
  }

  @Test
  public void testObjectFieldStable() {
    Function<A, String> xField = StableField.getter(lookup(), A.class, "x", String.class);
    A a = new A();
    assertNull(xField.apply(a));
    a.x = "hello";
    assertEquals("hello", xField.apply(a));
    assertEquals("hello", xField.apply(a));
  }

  @Test
  public void testObjectFieldStableStill() {
    Function<A, String> xField = StableField.getter(lookup(), A.class, "x", String.class);
    A a = new A();
    assertNull(xField.apply(a));
    a.x = "hello";
    assertEquals("hello", xField.apply(a));
    a.x = "banzai";
    assertEquals("hello", xField.apply(a));
  }

  @Test
  public void testObjectFieldNonConstant() {
    Function<A, String> xField = StableField.getter(lookup(), A.class, "x", String.class);
    A a1 = new A();
    a1.x = "foo";
    assertEquals("foo", xField.apply(a1));
    A a2 = new A();
    assertThrows(IllegalStateException.class, () -> xField.apply(a2));
  }

  static class B {
    int y;
  }

  @Test
  public void testIntFieldUninitialized() {
    ToIntFunction<B> yField = StableField.intGetter(lookup(), B.class, "y");
    B b = new B();
    assertEquals(0, yField.applyAsInt(b));
    assertEquals(0, yField.applyAsInt(b));
  }

  @Test
  public void testIntFieldStable() {
    ToIntFunction<B> yField = StableField.intGetter(lookup(), B.class, "y");
    B b = new B();
    assertEquals(0, yField.applyAsInt(b));
    b.y = 42;
    assertEquals(42, yField.applyAsInt(b));
    assertEquals(42, yField.applyAsInt(b));
  }

  @Test
  public void testIntFieldStableStill() {
    ToIntFunction<B> yField = StableField.intGetter(lookup(), B.class, "y");
    B b = new B();
    assertEquals(0, yField.applyAsInt(b));
    b.y = 42;
    assertEquals(42, yField.applyAsInt(b));
    b.y = 777;
    assertEquals(42, yField.applyAsInt(b));
  }

  @Test
  public void testIntFieldNonConstant() {
    ToIntFunction<B> yField = StableField.intGetter(lookup(), B.class, "y");
    B b1 = new B();
    b1.y = 666;
    assertEquals(666, yField.applyAsInt(b1));
    B b2 = new B();
    assertThrows(IllegalStateException.class, () -> yField.applyAsInt(b2));
  }

  static class C {
    long z;
  }

  @Test
  public void testLongFieldUninitialized() {
    ToLongFunction<C> zField = StableField.longGetter(lookup(), C.class, "z");
    C c = new C();
    assertEquals(0, zField.applyAsLong(c));
    assertEquals(0, zField.applyAsLong(c));
  }

  @Test
  public void testLongFieldStable() {
    ToLongFunction<C> zField = StableField.longGetter(lookup(), C.class, "z");
    C c = new C();
    assertEquals(0L, zField.applyAsLong(c));
    c.z = 42L;
    assertEquals(42L, zField.applyAsLong(c));
    assertEquals(42L, zField.applyAsLong(c));
  }

  @Test
  public void testLongFieldStableStill() {
    ToLongFunction<C> zField = StableField.longGetter(lookup(), C.class, "z");
    C c = new C();
    assertEquals(0, zField.applyAsLong(c));
    c.z = 42L;
    assertEquals(42L, zField.applyAsLong(c));
    c.z = 777L;
    assertEquals(42L, zField.applyAsLong(c));
  }

  @Test
  public void tesLongFieldNonConstant() {
    ToLongFunction<C> zField = StableField.longGetter(lookup(), C.class, "z");
    C c1 = new C();
    c1.z = 666;
    assertEquals(666, zField.applyAsLong(c1));
    C c2 = new C();
    assertThrows(IllegalStateException.class, () -> zField.applyAsLong(c2));
  }

  static class D {
    double z;
  }

  @Test
  public void testDoubleFieldUninitialized() {
    ToDoubleFunction<D> zField = StableField.doubleGetter(lookup(), D.class, "z");
    D d = new D();
    assertEquals(0.0, zField.applyAsDouble(d));
    assertEquals(0.0, zField.applyAsDouble(d));
  }

  @Test
  public void testDoubleFieldStable() {
    ToDoubleFunction<D> zField = StableField.doubleGetter(lookup(), D.class, "z");
    D d = new D();
    assertEquals(0.0, zField.applyAsDouble(d));
    d.z = 42.0;
    assertEquals(42.0, zField.applyAsDouble(d));
    assertEquals(42.0, zField.applyAsDouble(d));
  }

  @Test
  public void testDoubleFieldStableStill() {
    ToDoubleFunction<D> zField = StableField.doubleGetter(lookup(), D.class, "z");
    D d = new D();
    assertEquals(0.0, zField.applyAsDouble(d));
    d.z = 42;
    assertEquals(42.0, zField.applyAsDouble(d));
    d.z = 777;
    assertEquals(42.0, zField.applyAsDouble(d));
  }

  @Test
  public void testDoubleFieldNonConstant() {
    ToDoubleFunction<D> zField = StableField.doubleGetter(lookup(), D.class, "z");
    D d1 = new D();
    d1.z = 666.0;
    assertEquals(666.0, zField.applyAsDouble(d1));
    D d2 = new D();
    assertThrows(IllegalStateException.class, () -> zField.applyAsDouble(d2));
  }

  static class Boxed {
    int i;
    long j;
    double d;
  }

  @Test
  public void testBoxedIntField() {
    Function<Boxed, Integer> iField = StableField.getter(lookup(), Boxed.class, "i", int.class);
    Boxed boxed = new Boxed();
    assertEquals(0, (int) iField.apply(boxed));
    boxed.i = 3;
    assertEquals(3, (int) iField.apply(boxed));
    boxed.i = 5;
    assertEquals(3, (int) iField.apply(boxed));
    assertThrows(IllegalStateException.class, () -> iField.apply(new Boxed()));
  }

  @Test
  public void testBoxedLongField() {
    Function<Boxed, Long> jField = StableField.getter(lookup(), Boxed.class, "j", long.class);
    Boxed boxed = new Boxed();
    assertEquals(0L, (long) jField.apply(boxed));
    boxed.j = 3L;
    assertEquals(3L, (long) jField.apply(boxed));
    boxed.j = 5L;
    assertEquals(3L, (long) jField.apply(boxed));
    assertThrows(IllegalStateException.class, () -> jField.apply(new Boxed()));
  }

  @Test
  public void testBoxedDoubleField() {
    Function<Boxed, Double> dField = StableField.getter(lookup(), Boxed.class, "d", double.class);
    Boxed boxed = new Boxed();
    assertEquals(0.0, (double) dField.apply(boxed));
    boxed.d = 3.0;
    assertEquals(3.0, (double) dField.apply(boxed));
    boxed.d = 5.0;
    assertEquals(3.0, (double) dField.apply(boxed));
    assertThrows(IllegalStateException.class, () -> dField.apply(new Boxed()));
  }

  static class Prim {
    boolean z;
    byte b;
    char c;
    short s;
    float f;
  }

  @Test
  public void testPrimBooleanField() {
    Function<Prim, Boolean> zField = StableField.getter(lookup(), Prim.class, "z", boolean.class);
    Prim prim = new Prim();
    assertFalse(zField.apply(prim));
    prim.z = true;
    assertTrue(zField.apply(prim));
    prim.z = false;
    assertTrue(zField.apply(prim));
    assertThrows(IllegalStateException.class, () -> zField.apply(new Prim()));
  }

  @Test
  public void testPrimByteField() {
    Function<Prim, Byte> bField = StableField.getter(lookup(), Prim.class, "b", byte.class);
    Prim prim = new Prim();
    assertEquals((byte) 0, (byte) bField.apply(prim));
    prim.b = 10;
    assertEquals((byte) 10, (byte) bField.apply(prim));
    prim.b = 5;
    assertEquals((byte) 10, (byte) bField.apply(prim));
    assertThrows(IllegalStateException.class, () -> bField.apply(new Prim()));
  }

  @Test
  public void testPrimCharField() {
    Function<Prim, Character> cField = StableField.getter(lookup(), Prim.class, "c", char.class);
    Prim prim = new Prim();
    assertEquals((char) 0, (char) cField.apply(prim));
    prim.c = 'A';
    assertEquals('A', (char) cField.apply(prim));
    prim.c = 'B';
    assertEquals('A', (char) cField.apply(prim));
    assertThrows(IllegalStateException.class, () -> cField.apply(new Prim()));
  }

  @Test
  public void testPrimShortField() {
    Function<Prim, Short> sField = StableField.getter(lookup(), Prim.class, "s", short.class);
    Prim prim = new Prim();
    assertEquals((short) 0, (short) sField.apply(prim));
    prim.s = 1_000;
    assertEquals((short) 1_000, (short) sField.apply(prim));
    prim.s = 2_000;
    assertEquals((short) 1_000, (short) sField.apply(prim));
    assertThrows(IllegalStateException.class, () -> sField.apply(new Prim()));
  }

  @Test
  public void testPrimFloatField() {
    Function<Prim, Float> fField = StableField.getter(lookup(), Prim.class, "f", float.class);
    Prim prim = new Prim();
    assertEquals(0.0f, (float) fField.apply(prim));
    prim.f = 0.2f;
    assertEquals(0.2f, (float) fField.apply(prim));
    prim.f = 0.4f;
    assertEquals(0.2f, (float) fField.apply(prim));
    assertThrows(IllegalStateException.class, () -> fField.apply(new Prim()));
  }

  @Test
  public void testNoSuchField() {
    assertThrows(
        NoSuchFieldError.class,
        () -> StableField.getter(lookup(), Object.class, "foo", String.class));
    assertThrows(
        NoSuchFieldError.class, () -> StableField.intGetter(lookup(), Object.class, "foo"));
    assertThrows(
        NoSuchFieldError.class, () -> StableField.longGetter(lookup(), Object.class, "foo"));
    assertThrows(
        NoSuchFieldError.class, () -> StableField.doubleGetter(lookup(), Object.class, "foo"));
  }

  private static class Foo {
    private @SuppressWarnings("unused") Object a;
    private @SuppressWarnings("unused") int b;
    private @SuppressWarnings("unused") long c;
    private @SuppressWarnings("unused") double d;
  }

  @Test
  public void testNoAccess() {
    assertThrows(
        IllegalAccessError.class,
        () -> StableField.getter(publicLookup(), Foo.class, "a", Object.class));
    assertThrows(
        IllegalAccessError.class, () -> StableField.intGetter(publicLookup(), Foo.class, "b"));
    assertThrows(
        IllegalAccessError.class, () -> StableField.longGetter(publicLookup(), Foo.class, "c"));
    assertThrows(
        IllegalAccessError.class, () -> StableField.doubleGetter(publicLookup(), Foo.class, "d"));
  }
}
