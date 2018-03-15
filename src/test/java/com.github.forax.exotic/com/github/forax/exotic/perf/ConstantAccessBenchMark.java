package com.github.forax.exotic.perf;

import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

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

import com.github.forax.exotic.MostlyConstant;

@SuppressWarnings("static-method")
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ConstantAccessBenchMark {
  static final int static_final_int = 1_000;
  static final Integer static_final_Integer = 1_000;
  
  private static final MostlyConstant<Integer> MOSTLY_CONSTANT_INT = new MostlyConstant<>(1_000, int.class);
  private static final IntSupplier MOSTLY_CONSTANT_INT_GETTER = MOSTLY_CONSTANT_INT.intGetter();
  
  private static final MostlyConstant<Integer> MOSTLY_CONSTANT_INTEGER = new MostlyConstant<>(1_000, Integer.class);
  private static final Supplier<Integer> MOSTLY_CONSTANT_INTEGER_GETTER = MOSTLY_CONSTANT_INTEGER.getter();
  
  @Benchmark
  public int static_final_int() {
    return 1_000 / static_final_int;
  }
  
  @Benchmark
  public int static_final_Integer() {
    return 1_000 / static_final_Integer;
  }

  @Benchmark
  public int mostly_constant_int() {
    return 1_000 / MOSTLY_CONSTANT_INT_GETTER.getAsInt();
  }
  
  @Benchmark
  public int mostly_constant_Integer() {
    return 1_000 / MOSTLY_CONSTANT_INTEGER_GETTER.get();
  }
  
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(ConstantAccessBenchMark.class.getName())
        .build();
    new Runner(opt).run();
  }
}