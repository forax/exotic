package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class VisitorTests {
  
  interface Expr { /**/ }
  static class Value implements Expr { final int value; Value(int value) { this.value = value; }}
  static class Add implements Expr { final Expr left, right; Add(Expr left, Expr right) { this.left = left; this.right = right; }}
  static class Var implements Expr { final String name; Var(String name) { this.name = name; }}
  static class Assign implements Expr { final String name; final Expr expr; Assign(String name, Expr expr) { this.name = name; this.expr = expr; }}
  static class Block implements Expr { final List<Expr> exprs; Block(List<Expr> exprs) { this.exprs = exprs; }}
  
  @Test
  void simple() {
    Visitor<Void, Integer> visitor = Visitor.create(Void.class, int.class, opt -> opt
        .register(Value.class, (v, value, __) -> value.value)
        .register(Add.class,   (v, add, __)   -> v.visit(add.left, null) + v.visit(add.right, null))
        );
    Expr expr = new Add(new Add(new Value(7), new Value(10)), new Value(4));
    assertEquals(21, (int)visitor.visit(expr, null));
  }
  
  @Test
  void interpret() {
    class Env {
      final HashMap<String, Integer> vars = new HashMap<>();
    }
    
    Visitor<Env, Integer> visitor = Visitor.create(Env.class, int.class, opt -> opt
        .register(Value.class,  (v, value, env)  -> value.value)
        .register(Add.class,    (v, add, env)    -> v.visit(add.left, env) + v.visit(add.right, env))
        .register(Var.class,    (v, var, env)    -> env.vars.getOrDefault(var.name, 0))
        .register(Assign.class, (v, assign, env) -> { int let = v.visit(assign.expr, env); env.vars.put(assign.name, let); return let; })
        .register(Block.class,  (v, block, env)  -> block.exprs.stream().mapToInt(e -> v.visit(e, env)).reduce(0, (v1, v2) -> v2))
        );
    Expr code = new Block(List.of(
        new Assign("a", new Value(3)),
        new Add(new Add(new Var("a"), new Value(10)), new Value(4))
        ));
    assertEquals(17, (int)visitor.visit(code, new Env()));
  }
  
  @Test
  void registerSameVisitletTwice() {
    assertThrows(IllegalStateException.class, () ->
        Visitor.create(Void.class, Void.class, opt -> opt
            .register(String.class, (_1, _2, _3) -> null)
            .register(String.class, (_1, _2, _3) -> null)
            ));
  }
  
  @Test
  void visitCanNotFindAVisitlet() {
    Visitor<Void,Void> visitor = Visitor.create(Void.class, Void.class, opt -> { /*empty*/ });
    assertThrows(IllegalStateException.class, () ->
        visitor.visit("oops", null));
  }
  
  @Test
  void nullWhenCreatingAVisitor() {
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> Visitor.create(null, Void.class, opt -> { /*empty*/ })),
        () -> assertThrows(NullPointerException.class, () -> Visitor.create(Void.class, null, opt -> { /*empty*/ })),
        () -> assertThrows(NullPointerException.class, () -> Visitor.create(Void.class, Void.class, null))
        );
  }
  
  @Test
  void nullWhenCallingVisit() {
    Visitor<String, Void> visitor = Visitor.create(String.class, Void.class, opt -> { /*empty*/ });
    assertThrows(NullPointerException.class, () -> visitor.visit(null, "hello"));
  }
}
