package com.github.forax.exotic.perf;

import static java.lang.invoke.MethodHandles.lookup;

import java.util.Objects;
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

import com.github.forax.exotic.ObjectSupport;

@SuppressWarnings("static-method")
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ObjectSupportBenchMark {
  static final class AutoPerson {
    private static final ObjectSupport<AutoPerson> SUPPORT = ObjectSupport.of(lookup(), AutoPerson.class, "name", "age");
    
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final int age;

    public AutoPerson(String name, int age) {
      this.name = name;
      this.age = age;
    }
    
    @Override
    public boolean equals(Object other) {
      return SUPPORT.equals(this, other);
    }
    
    @Override
    public int hashCode() {
      return SUPPORT.hashCode(this);
    }
  }
  
  static final class HandWrittenPerson {
    private final String name;
    private final int age;

    public HandWrittenPerson(String name, int age) {
      this.name = name;
      this.age = age;
    }
    
    @Override
    public boolean equals(Object other) {
      if (!(other instanceof HandWrittenPerson)) {
        return false;
      }
      HandWrittenPerson person = (HandWrittenPerson) other;
      return Objects.equals(name, person.name) && age == person.age;
    }
    
    @Override
    public int hashCode() {
      return (1 * 63 + Objects.hashCode(name)) * 63 + age;
    }
  }

  private static final AutoPerson AUTO_PERSON1 = new AutoPerson("martin", 68);
  private static final AutoPerson AUTO_PERSON2 = new AutoPerson("martin", 68);
  private static final HandWrittenPerson HAND_WRITTEN_PERSON1 = new HandWrittenPerson("martin", 68);
  private static final HandWrittenPerson HAND_WRITTEN_PERSON2 = new HandWrittenPerson("martin", 68);
  
  @Benchmark
  public boolean auto_equals() {
    return AUTO_PERSON1.equals(AUTO_PERSON2);
  }

  @Benchmark
  public boolean hand_written_equals() {
    return HAND_WRITTEN_PERSON1.equals(HAND_WRITTEN_PERSON2);
  }
  
  @Benchmark
  public int auto_hashCode() {
    return AUTO_PERSON1.hashCode();
  }

  @Benchmark
  public int hand_written_hashCode() {
    return HAND_WRITTEN_PERSON1.hashCode();
  }
  

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(ObjectSupportBenchMark.class.getName()).build();
    new Runner(opt).run();
  }
}
