package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class StableFieldExample2Tests {
  enum Option {
    a,
    b;

    private static final Function<Option, String> UPPERCASE =
        StableField.getter(lookup(), Option.class, "uppercase", String.class);

    @SuppressWarnings("unused")
    private String uppercase; // stable

    public String upperCase() {
      String uppercase = UPPERCASE.apply(this);
      if (uppercase != null) {
        return uppercase;
      }
      return this.uppercase = name().toUpperCase();
    }
  }

  @Test
  public void test() {
    assertEquals("A", Option.a.upperCase());
    assertEquals("B", Option.b.upperCase());
  }
}
