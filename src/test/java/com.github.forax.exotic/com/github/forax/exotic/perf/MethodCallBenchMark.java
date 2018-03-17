package com.github.forax.exotic.perf;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import com.github.forax.exotic.ConstantMemoizer;
import com.github.forax.exotic.StructuralCall;
import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;
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

@SuppressWarnings("static-method")
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class MethodCallBenchMark {
  interface I {
    int f();
  }

  static class A implements I {
    @Override
    public int f() {
      return 1;
    }
  }

  static class B implements I {
    @Override
    public int f() {
      return 1;
    }
  }

  static class C implements I {
    @Override
    public int f() {
      return 1;
    }
  }

  private static final I[] ARRAY = new I[] {new A(), new B(), new C()};

  private static final ToIntFunction<I> MEMOIZER = ConstantMemoizer.intMemoizer(I::f, I.class);

  private static final StructuralCall STRUCTURAL_CALL =
      StructuralCall.create(lookup(), "f", methodType(int.class));

  @Benchmark
  public int virtual_call() {
    int sum = 0;
    for (I i : ARRAY) {
      sum += i.f();
    }
    return sum;
  }

  @Benchmark
  public int memoizer() {
    int sum = 0;
    for (I i : ARRAY) {
      sum += MEMOIZER.applyAsInt(i);
    }
    return sum;
  }

  @Benchmark
  public int structural_call() {
    int sum = 0;
    for (I i : ARRAY) {
      sum += (int) STRUCTURAL_CALL.invoke(i);
    }
    return sum;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(MethodCallBenchMark.class.getName()).build();
    new Runner(opt).run();
  }
}
