package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class StructuralCallTests {
  @Test
  void simple() {
    StructuralCall call = StructuralCall.create(lookup(), "toString", methodType(String.class));
    assertEquals("mirror", call.invoke("mirror"));
    assertEquals("14", call.invoke(14));
    assertEquals("5.0", call.invoke(5.0));
  }

  @Test
  void comparable() {
    StructuralCall call =
        StructuralCall.create(lookup(), "compareTo", methodType(int.class, Object.class));
    assertEquals(0, (int) call.invoke("foo", "foo"));
    assertEquals(0, (int) call.invoke(14, 14));
    assertEquals(0, (int) call.invoke(LocalTime.of(14, 12), LocalTime.of(14, 12)));
  }

  @Test
  void wrongConfiguration() {
    assertThrows(
        NullPointerException.class,
        () -> StructuralCall.create(null, "foo", methodType(void.class)));
    assertThrows(
        NullPointerException.class,
        () -> StructuralCall.create(lookup(), null, methodType(void.class)));
    assertThrows(NullPointerException.class, () -> StructuralCall.create(lookup(), "foo", null));
  }

  static class NoAccess {
    @SuppressWarnings("unused")
    private String m(String s) {
      return s;
    }
  }

  @Test
  void cannotAccessToAPrivateMethod() {
    StructuralCall call =
        StructuralCall.create(lookup(), "m", methodType(String.class, String.class));
    assertThrows(IllegalAccessError.class, () -> call.invoke(new NoAccess(), "test"));
  }

  static class WrongLookup {
    long m(double d) {
      return (long) d;
    }
  }

  @Test
  void publicLookupCanNotAccessPackageMethod() {
    StructuralCall call =
        StructuralCall.create(publicLookup(), "m", methodType(long.class, double.class));
    assertThrows(IllegalAccessError.class, () -> call.invoke(new WrongLookup(), 4.0));
  }

  static class NotFound {
    /* empty */
  }

  @Test
  void noMethodDefined() {
    StructuralCall call =
        StructuralCall.create(lookup(), "m", methodType(String.class, String.class));
    assertThrows(NoSuchMethodError.class, () -> call.invoke(new NotFound(), "whereAreYou"));
  }

  @Test
  void accessMethodThroughInterface() {
    StructuralCall call = StructuralCall.create(lookup(), "isEmpty", methodType(boolean.class));
    assertEquals(false, (boolean) call.invoke(List.of(1, 2, 3)));
  }

  @SuppressWarnings("unused")
  static class WrongParameters {
    void m(boolean b) {
      /* empty */
    }

    void m(int i) {
      /* empty */
    }

    void m(double d) {
      /* empty */
    }
  }

  @Test
  void callingAMethodWithTheWrongClass() {
    WrongParameters wrongParameters = new WrongParameters();
    StructuralCall call1 =
        StructuralCall.create(lookup(), "m", methodType(void.class, boolean.class));
    assertThrows(ClassCastException.class, () -> call1.invoke(wrongParameters, "oops"));
    StructuralCall call2 = StructuralCall.create(lookup(), "m", methodType(void.class, int.class));
    assertThrows(ClassCastException.class, () -> call2.invoke(wrongParameters, "oops"));
    StructuralCall call3 =
        StructuralCall.create(lookup(), "m", methodType(void.class, double.class));
    assertThrows(ClassCastException.class, () -> call3.invoke(wrongParameters, "oops"));
  }

  static class WrongNumberOfArguments {
    long m(int i, long l) {
      return i + l;
    }
  }

  @Test
  void callingAMethodWithTheWrongNumberOfArguments() {
    WrongNumberOfArguments wrongNumberOfArguments = new WrongNumberOfArguments();
    StructuralCall call =
        StructuralCall.create(
            lookup(), "m", methodType(long.class, int.class, long.class)); // 2 parameters
    assertThrows(
        IllegalArgumentException.class, () -> call.invoke(wrongNumberOfArguments)); // 0 argument
    assertThrows(
        IllegalArgumentException.class, () -> call.invoke(wrongNumberOfArguments, 0)); // 1 argument
    assertThrows(
        IllegalArgumentException.class,
        () -> call.invoke(wrongNumberOfArguments, 0, 0, 0)); // 3 argument
    assertThrows(
        IllegalArgumentException.class,
        () -> call.invoke(wrongNumberOfArguments, 0, 0, 0, 0)); // 4 argument
    assertThrows(
        IllegalArgumentException.class,
        () -> call.invoke(wrongNumberOfArguments, 0, 0, 0, 0, 0)); // 5 argument
    assertThrows(
        IllegalArgumentException.class,
        () -> call.invoke(wrongNumberOfArguments, 0, 0, 0, 0, 0, 0)); // 6 argument
    assertThrows(
        IllegalArgumentException.class,
        () -> call.invoke(wrongNumberOfArguments, 0, 0, 0, 0, 0, 0, 0)); // 7 argument
    assertThrows(
        IllegalArgumentException.class,
        () -> call.invoke(wrongNumberOfArguments, 0, 0, 0, 0, 0, 0, 0, 0)); // 8 argument
  }
}
