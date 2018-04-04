package com.github.forax.exotic.perf;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

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

import com.github.forax.exotic.Visitor;

@SuppressWarnings("static-method")
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class VisitorBenchMark {
  
  interface Expr {
    <P, R> R accept(GofVisitor<? super P, ? extends R> visitor, P parameter);
  }
  static class Value implements Expr {
    final int value; Value(int value) { this.value = value; }
    @Override
    public <P, R> R accept(GofVisitor<? super P, ? extends R> visitor, P parameter) {
      return visitor.visitValue(this, parameter);
    }
  }
  static class Add implements Expr {
    final Expr left, right; Add(Expr left, Expr right) { this.left = left; this.right = right; }
    @Override
    public <P, R> R accept(GofVisitor<? super P, ? extends R> visitor, P parameter) {
      return visitor.visitAdd(this, parameter);
    }  
  }
  static class Var implements Expr {
    final String name; Var(String name) { this.name = name; }
    @Override
    public <P, R> R accept(GofVisitor<? super P, ? extends R> visitor, P parameter) {
      return visitor.visitVar(this, parameter);
    }  
  }
  static class Assign implements Expr {
    final String name; final Expr expr; Assign(String name, Expr expr) { this.name = name; this.expr = expr; }
    @Override
    public <P, R> R accept(GofVisitor<? super P, ? extends R> visitor, P parameter) {
      return visitor.visitAssign(this, parameter);
    }  
  }
  static class Block implements Expr {
    final List<Expr> exprs; Block(List<Expr> exprs) { this.exprs = exprs; }
    @Override
    public <P, R> R accept(GofVisitor<? super P, ? extends R> visitor, P parameter) {
      return visitor.visitBlock(this, parameter);
    }
  }

  interface GofVisitor<P, R> {
    R visitValue(Value value, P parameter);
    R visitAdd(Add add, P parameter);
    R visitVar(Var var, P parameter);
    R visitAssign(Assign assign, P parameter);
    R visitBlock(Block block, P parameter);
  }
  
  static class MapVisitor<P, R> {
    private final HashMap<Class<?>, BiFunction<Object, P, R>> map = new HashMap<>();
    
    @SuppressWarnings("unchecked")
    public <T> MapVisitor<P, R> register(Class<T> type, BiFunction<? super T, ? super P, ? extends R> fun) {
      map.put(type, (BiFunction<Object, P, R>)fun);
      return this;
    }
    
    public R visit(Object expr, P parameter) {
      return map.getOrDefault(expr.getClass(), (_1, _2) -> { throw new IllegalStateException(); }).apply(expr, parameter);
    }
  }
  
  class Env {
    final HashMap<String, Integer> vars = new HashMap<>();
  }
  
  private static final Expr CODE = new Block(List.of(
      new Assign("c", new Block(List.of(
          new Assign("a", new Value(20)),
          new Var("e"),
          new Assign("b", new Add(new Value(5), new Var("c"))),
          new Assign("d", new Var("b")),
          new Add(new Add(new Var("a"), new Value(11)), new Var("d"))
          ))
      ),
      new Add(new Value(2), new Var("b"))
    ));
  
  private static final Visitor<Env, Integer> EXOTIC_VISITOR = Visitor.create(Env.class, int.class, opt -> opt
      .register(Value.class,  (v, value, env)  -> value.value)
      .register(Add.class,    (v, add, env)    -> v.visit(add.left, env) + v.visit(add.right, env))
      .register(Var.class,    (v, var, env)    -> env.vars.getOrDefault(var.name, 0))
      .register(Assign.class, (v, assign, env) -> { int let = v.visit(assign.expr, env); env.vars.put(assign.name, let); return let; })
      .register(Block.class,  (v, block, env)  -> { int result = 0; for(Expr expr: block.exprs) { result = v.visit(expr, env); } return result; })
      );

  private static final GofVisitor<Env, Integer> GOF_VISITOR = new GofVisitor<>() {
    @Override
    public Integer visitValue(Value value, Env env) { return value.value; }
    @Override
    public Integer visitAdd(Add add, Env env) { return add.left.accept(this, env) + add.right.accept(this, env); }
    @Override
    public Integer visitVar(Var var, Env env) { return env.vars.getOrDefault(var.name, 0); }
    @Override
    public Integer visitAssign(Assign assign, Env env) { int let = assign.expr.accept(this, env); env.vars.put(assign.name, let); return let; }
    @Override
    public Integer visitBlock(Block block, Env env) { int result = 0; for(Expr expr: block.exprs) { result = expr.accept(this, env); } return result; }
  };
  
  private static final MapVisitor<Env, Integer> MAP_VISITOR = new MapVisitor<>();
  static {
    MAP_VISITOR.register(Value.class,  (value, env)  -> value.value)
        .register(Add.class,    (add, env)    -> MAP_VISITOR.visit(add.left, env) + MAP_VISITOR.visit(add.right, env))
        .register(Var.class,    (var, env)    -> env.vars.getOrDefault(var.name, 0))
        .register(Assign.class, (assign, env) -> { int let = MAP_VISITOR.visit(assign.expr, env); env.vars.put(assign.name, let); return let; })
        .register(Block.class,  (block, env)  -> { int result = 0; for(Expr expr: block.exprs) { result = MAP_VISITOR.visit(expr, env); } return result; });
  }
     
  
  @Benchmark
  public int gof_visitor() {
    return MAP_VISITOR.visit(CODE, new Env());
  }
  
  @Benchmark
  public int exotic_visitor() {
    return EXOTIC_VISITOR.visit(CODE, new Env());
  }

  @Benchmark
  public int map_visitor() {
    return CODE.accept(GOF_VISITOR, new Env());
  }
  

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(VisitorBenchMark.class.getName()).build();
    new Runner(opt).run();
  }
}
