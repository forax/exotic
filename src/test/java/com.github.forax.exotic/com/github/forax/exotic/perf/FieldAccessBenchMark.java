package com.github.forax.exotic.perf;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntSupplier;
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

import com.github.forax.exotic.ConstantMemoizer;
import com.github.forax.exotic.MostlyConstant;
import com.github.forax.exotic.StableField;
import com.github.forax.exotic.StructuralCall;

@SuppressWarnings("static-method")
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class FieldAccessBenchMark {
  static final A static_final = new A(1_000);

  static class A {
    final int x;

    public A(int x) {
      this.x = x;
    }
    
    public int x() {
      return x;
    }
  }
  
  private static final MostlyConstant<Integer> MOSTLY_CONSTANT = new MostlyConstant<>(1_000, int.class);
  private static final IntSupplier MOSTLY_CONSTANT_GETTER = MOSTLY_CONSTANT.intGetter();
  
  private static final ToIntFunction<A> STABLE_X =
      StableField.intGetter(lookup(), A.class, "x");

  private static final Function<A, Integer> MEMOIZER =
      ConstantMemoizer.memoizer(A::x, A.class, int.class);
  
  private final static StructuralCall STRUCTURAL_CALL =
      StructuralCall.create(lookup(), "x", methodType(int.class));
  
  @Benchmark
  public int field_access() {
    return 1_000 / static_final.x;
  }

  @Benchmark
  public int mostly_constant() {
    return 1_000 / MOSTLY_CONSTANT_GETTER.getAsInt();
  }
  
  @Benchmark
  public int stable_field() {
    return 1_000 / STABLE_X.applyAsInt(static_final);
  }
  
  @Benchmark
  public int memoizer() {
    return 1_000 / MEMOIZER.apply(static_final);
  }
  
  @Benchmark
  public int structural_call() {
    return 1_000 / (int)STRUCTURAL_CALL.invoke(static_final);
  }
  
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(FieldAccessBenchMark.class.getName())
        .build();
    new Runner(opt).run();
  }
}