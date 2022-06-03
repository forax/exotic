package com.github.forax.exotic.perf;

import java.net.URI;
import java.nio.CharBuffer;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.github.forax.exotic.TypeSwitch;

@SuppressWarnings("static-method")
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class TypeSwitchBenchMark {
  
  interface I { /* empty */ }
  interface J { /* empty */ }
  static class A { /* empty */ }
  static class B implements I { /* empty */ }
  static class C implements J { /* empty */ }
  static class D implements I, J { /* empty */ }
  static class E implements I { /* empty */ }
  static class F implements J { /* empty */ }
  
  private static final TypeSwitch SMALL_TYPE_SWITCH = TypeSwitch.create(true,
      D.class, C.class, B.class, A.class/*, J.class, I.class*/);
  
  private static final TypeSwitch BIG_TYPE_SWITCH = TypeSwitch.create(true,
      D.class, C.class, B.class, A.class, J.class, I.class,
      String.class, StringBuilder.class, CharSequence.class, URI.class, LocalDate.class, Comparable.class, Object.class);
     
  private static final Object[] DATA = {
    new D(), new E(), new C(), new A(), new F(), new B(), new A(), new E(), new F(), new D() { /*empty*/}, new A() { /*empty*/ },
    "hello", new StringBuilder("hello"), CharBuffer.wrap("hello"),
    LocalDate.now(), new Date(), A.class
  };
  
  @Benchmark
  public int small_small_type_switch() {
    int sum = 0;
    for(int i = 0; i < 4; i++) {
      Object o = DATA[i];
      sum += SMALL_TYPE_SWITCH.typeSwitch(o);
    }
    return sum;
  }
  
  @Benchmark
  public int small_small_instanceof_cascade() {
    int sum = 0;
    for(int i = 0; i < 4; i++) {
      Object o = DATA[i];
      int value;
      if (o == null) { value = TypeSwitch.NULL_MATCH; } else
        if (o instanceof D) { value = 0; } else
          if (o instanceof C) { value = 1; } else
            if (o instanceof B) { value = 2; } else
              if (o instanceof A) { value = 3; } else
                  { value = TypeSwitch.NO_MATCH; }
      sum += value;
    }
    return sum;
  }
  
  @Benchmark
  public int small_big_type_switch() {
    int sum = 0;
    for(Object o: DATA) {
      sum += SMALL_TYPE_SWITCH.typeSwitch(o);
    }
    return sum;
  }
  
  @Benchmark
  public int small_big_instanceof_cascade() {
    int sum = 0;
    for(Object o: DATA) {
      int value;
      if (o == null) { value = TypeSwitch.NULL_MATCH; } else
        if (o instanceof D) { value = 0; } else
          if (o instanceof C) { value = 1; } else
            if (o instanceof B) { value = 2; } else
              if (o instanceof A) { value = 3; } else
                  { value = TypeSwitch.NO_MATCH; }
      sum += value;
    }
    return sum;
  }
  
  @Benchmark
  public int big_big_type_switch() {
    int sum = 0;
    for(Object o: DATA) {
      sum += BIG_TYPE_SWITCH.typeSwitch(o);
    }
    return sum;
  }
  
  @Benchmark
  public int big_big_instanceof_cascade() {
    int sum = 0;
    for(Object o: DATA) {
      int value;
      if (o == null) { value = TypeSwitch.NULL_MATCH; } else
        if (o instanceof D) { value = 0; } else
          if (o instanceof C) { value = 1; } else
            if (o instanceof B) { value = 2; } else
              if (o instanceof A) { value = 3; } else
                if (o instanceof J) { value = 4; } else
                  if (o instanceof I) { value = 5; } else
                    if (o instanceof String) { value = 6; } else
                      if (o instanceof StringBuilder) { value = 7; } else
                        if (o instanceof CharSequence) { value = 8; } else
                          if (o instanceof URI) { value = 9; } else
                            if (o instanceof LocalDate) { value = 10; } else
                              if (o instanceof Comparable) { value = 11; } else
                                if (o instanceof Object) { value = 12; } else
                                { value = TypeSwitch.NO_MATCH; }
      sum += value;
    }
    return sum;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(TypeSwitchBenchMark.class.getName()).build();
    new Runner(opt).run();
  }
}
