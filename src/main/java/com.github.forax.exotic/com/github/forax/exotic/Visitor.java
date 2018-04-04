package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A visitor based on lambdas that tries hard to inline all intra-visitor calls. 
 *
 * Let suppose we have the following AST (abstract syntax tree),
 * <pre>
 * interface Expr { }
 * class Value implements Expr { final int value; Value(int value) { this.value = value; }}
 * class Add implements Expr { final Expr left, right; Add(Expr left, Expr right) { this.left = left; this.right = right; }}
 * </pre>
 * 
 * to evaluate it, we can use a visitor configured like this
 * <pre>
 *   private static final Visitor&lt;Void, Integer&gt; VISITOR = Visitor.create(Void.class, int.class, opt -&gt; opt
 *       .register(Value.class, (visitor, value, __) -&gt; value.value)
 *       .register(Add.class,   (visitor, add, __)   -&gt; visitor.visit(add.left, null) + visitor.visit(add.right, null))
 *       );
 *   ...
 *   Expr expr = new Add(new Add(new Value(7), new Value(10)), new Value(4));
 *   int value = VISITOR.visit(expr, null);  // 21
 * </pre>
 *
 * @param <P> type of the parameter value (the inherited attribute)
 * @param <R> type of the return value (the synthesized attribute)
 */
@FunctionalInterface
public interface Visitor<P, R> {
  /**
   * Visit one of the {@link Visitlet} depending on the class of the expression {@code expr}.
   * 
   * @param expr an expression
   * @param parameter a parameter or null.
   * @return the return value of the called {@link Visitlet}.
   * @throws NullPointerException if {@code expr} is null.
   * @throws IllegalStateException if the expression class has no corresponding visitlet defined
   */
  R visit(Object expr, P parameter);
  
  /**
   * A computation part of a visitor specific for a type.
   *
   * @param <T> the type of the expression.
   * @param <P> the type of the parameter, can be Void if the parameter is null.
   * @param <R> the type of the return value.
   * 
   * @see Registry#register(Class, Visitlet)
   * @see Visitor#visit(Object, Object)
   */
  @FunctionalInterface
  interface Visitlet<T, P, R> {
    /**
     * The computation for a part of an expression.
     * 
     * @param visitor a visitor that can be called to do a recursive computation.
     * @param expr an expression.
     * @param parameter the value of a parameter or null.
     * @return the value of the computation.
     */
    R visit(Visitor<P, R> visitor, T expr, P parameter);
  }
  
  /**
   * Registry that contains the association between a type and its corresponding computation as a @link {@link Visitlet}. 
   *
   * @param <P> the type of the parameter, can be Void if the parameter is null.
   * @param <R> the type of the return value.
   */
  interface Registry<P, R> {
    /**
     * Register a computation for a specific type.
     * 
     * @param <T> type of the expression.
     * @param type the class of the expression that will be computed by the computation.
     * @param visitlet a computation.
     * @return itself so calls to register can be chained (as a build).
     * @throws NullPointerException if the {@code type} or {@code visitlet} is null.
     * @throws IllegalStateException if a computation has already register for a type.
     */
    <T> Registry<P, R> register(Class<T> type, Visitlet<? super T, ? super P, ? extends R> visitlet);
  }
  
  /**
   * Creates a visitor with the {@link Visitlet visitlets} registered in the {@link Registry}. 
   * 
   * @param <P> type of the parameter, can be Void if the parameter is null.
   * @param <R> type of the return value.
   * @param pType class of the parameter type.
   * @param rType class of the return type.
   * @param consumer consumer that will register the {@link Visitlet visitlet} in the {@link Registry}.
   * @return a visitor configured with the {@link Visitlet visitlets}.
   * @throws NullPointerException if {@code pType}, {@code rType} or {@code consumer} is null.
   */
  static <P, R> Visitor<P, R> create(Class<P> pType, Class<R> rType, Consumer<? super Registry<P, R>> consumer) {
    Objects.requireNonNull(pType);
    Objects.requireNonNull(rType);
    Objects.requireNonNull(consumer);
    HashMap<Class<?>, MethodHandle> map = new HashMap<>();
    
    class RegistryImpl implements Registry<P, R> {
      @Override
      public <T> Registry<P, R> register(Class<T> type, Visitlet<? super T, ? super P, ? extends R> visitlet) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(visitlet);
        if (map.containsKey(type)) {
          throw new IllegalStateException("there is already a visitlet register for type " + type.getName());
        }
        MethodHandle mh = insertArguments(VisitorCallSite.VISIT, 0, visitlet, VisitorCallSite.visitor(pType, rType, map))
            .asType(methodType(rType, type, pType))
            .asType(methodType(rType, Object.class, pType));
        map.put(type, mh);
        return this;
      }
    }
    consumer.accept(new RegistryImpl());
    return VisitorCallSite.visitor(pType, rType, map);
  }
}
