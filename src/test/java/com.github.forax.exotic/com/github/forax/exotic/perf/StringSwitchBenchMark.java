package com.github.forax.exotic.perf;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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

import com.github.forax.exotic.StringSwitch;

@SuppressWarnings("static-method")
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class StringSwitchBenchMark {
  private static final StringSwitch SMALL_STRING_SWITCH = StringSwitch.create(false,
      "elephant", "girafe", "springbok", "monkey"/*, "snake", "crocodile", "orangoutang", "opossum", "tiger", "hippopotamus", "koala" */);
  
  private static final StringSwitch BIG_STRING_SWITCH = StringSwitch.create(false,
      "elephant", "girafe", "springbok", "monkey", "snake", "crocodile", "orangoutang", "opossum", "tiger", "hippopotamus", "koala");
     
  private static final String[] DATA = Stream.of(
      "lion", "elephant", "springbok", "elephant", "girafe", "snake", "crocodile", "elephant", "monkey",
      "girafe", "hippopotamus", "opossum", "elephant", "girafe", "snake", "opossum", "lion", "tiger", "snake", "koala")
      .map(String::new)
      .toArray(String[]::new);
  
  @Benchmark
  public int small_small_string_switch() {
    int sum = 0;
    for(int i = 0; i < 4; i++) {
      String s = DATA[i];
      sum += SMALL_STRING_SWITCH.stringSwitch(s);
    }
    return sum;
  }
  
  @Benchmark
  public int small_small_ifequals_cascade() {
    int sum = 0;
    for(int i = 0; i < 4; i++) {
      String s = DATA[i];
      int value;
      if (s.equals("elephant")) { value = 0; }
      else if (s.equals("girafe")) { value = 1; }
      else if (s.equals("springbok")) { value = 2; }
      else if (s.equals("monkey")) { value = 3; }
      else { value = StringSwitch.NO_MATCH; }
      sum += value;
    }
    return sum;
  }
  
  @Benchmark
  public int small_small_oldswitch() {
    int sum = 0;
    for(int i = 0; i < 4; i++) {
      String s = DATA[i];
      int value;
      switch(s) {
      case "elephant":
        value = 0;
        break;
      case "girafe":
        value = 1;
        break;
      case "springbok":
        value = 2;
        break;
      case "monkey":
        value = 3;
        break;
      default:
        value = StringSwitch.NO_MATCH;
      }
      sum += value;
    }
    return sum;
  }
  
  /*@Benchmark
  public int small_big_string_switch() {
    int sum = 0;
    for(String s: DATA) {
      sum += SMALL_STRING_SWITCH.stringSwitch(s);
    }
    return sum;
  }
  
  @Benchmark
  public int small_big_ifequals_cascade() {
    int sum = 0;
    for(int i = 0; i < 4; i++) {
      String s = DATA[i];
      int value;
      if (s.equals("elephant")) { value = 0; }
      else if (s.equals("girafe")) { value = 1; }
      else if (s.equals("springbok")) { value = 2; }
      else if (s.equals("monkey")) { value = 3; }
      else { value = StringSwitch.NO_MATCH; }
      sum += value;
    }
    return sum;
  }
  
  @Benchmark
  public int small_big_oldswitch() {
    int sum = 0;
    for(String s: DATA) {
      int value;
      switch(s) {
      case "elephant":
        value = 0;
        break;
      case "girafe":
        value = 1;
        break;
      case "springbok":
        value = 2;
        break;
      case "monkey":
        value = 3;
        break;
      default:
        value = StringSwitch.NO_MATCH;
      }
      sum += value;
    }
    return sum;
  }*/
  
  @Benchmark
  public int big_big_string_switch() {
    int sum = 0;
    for(String s: DATA) {
      sum += BIG_STRING_SWITCH.stringSwitch(s);
    }
    return sum;
  }
  
  @Benchmark
  public int big_big_ifequals_cascade() {
    int sum = 0;
    for(int i = 0; i < 4; i++) {
      String s = DATA[i];
      int value;
      if (s.equals("elephant")) { value = 0; }
      else if (s.equals("girafe")) { value = 1; }
      else if (s.equals("springbok")) { value = 2; }
      else if (s.equals("monkey")) { value = 3; }
      else if (s.equals("snake")) { value = 4; }
      else if (s.equals("crocodile")) { value = 5; }
      else if (s.equals("orangoutang")) { value = 6; }
      else if (s.equals("opossum")) { value = 7; }
      else if (s.equals("tiger")) { value = 8; }
      else if (s.equals("hippopotamus")) { value = 9; }
      else if (s.equals("koala")) { value = 10; }
      else { value = StringSwitch.NO_MATCH; }
      sum += value;
    }
    return sum;
  }
  
  @Benchmark
  public int big_big_oldswitch() {
    int sum = 0;
    for(String s: DATA) {
      int value;
      switch(s) {
      case "elephant":
        value = 0;
        break;
      case "girafe":
        value = 1;
        break;
      case "springbok":
        value = 2;
        break;
      case "monkey":
        value = 3;
        break;
      case "snake":
        value = 4;
        break;
      case "crocodile":
        value = 5;
        break;
      case "orangoutang":
        value = 6;
        break;
      case "opossum":
        value = 7;
        break;
      case "tiger":
        value = 8;
        break;
      case "hippopotamus":
        value = 9;
        break;
      case "koala":
        value = 10;
        break;
      default:
        value = StringSwitch.NO_MATCH;
      }
      sum += value;
    }
    return sum;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(StringSwitchBenchMark.class.getName()).build();
    new Runner(opt).run();
  }
}
