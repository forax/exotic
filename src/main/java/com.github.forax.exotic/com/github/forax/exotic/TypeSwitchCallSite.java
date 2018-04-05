package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;

class TypeSwitchCallSite extends MutableCallSite {
  private static final MethodType OBJECT_TO_INT = methodType(int.class, Object.class);
  private static final MethodHandle FALLBACK, TYPECHECK, GET, NULLCHECK;
  static {
    Lookup lookup = MethodHandles.lookup();
    try {
      FALLBACK = lookup.findVirtual(TypeSwitchCallSite.class, "fallback", OBJECT_TO_INT);
      TYPECHECK = lookup.findStatic(TypeSwitchCallSite.class, "typecheck", methodType(boolean.class, Class.class, Object.class));
      GET = lookup.findStatic(TypeSwitchCallSite.class, "get", methodType(int.class, ClassValue.class, Object.class));
      NULLCHECK = lookup.findStatic(Objects.class, "isNull", methodType(boolean.class, Object.class));
    } catch(NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }
  
  private static final int MAX_DEPTH = 8;
  
  private final int depth;
  private final TypeSwitchCallSite callsite;
  private final ClassValue<Integer> classValue;
  
  TypeSwitchCallSite(ClassValue<Integer> classValue) {
    super(OBJECT_TO_INT);
    this.depth = 0;
    this.callsite = this;
    this.classValue = classValue;
    setTarget(FALLBACK.bindTo(this));
  }
  
  private TypeSwitchCallSite(int depth, TypeSwitchCallSite callsite, ClassValue<Integer> classValue) {
    super(OBJECT_TO_INT);
    this.depth = depth;
    this.callsite = callsite;
    this.classValue = classValue;
    setTarget(FALLBACK.bindTo(this));
  }

  @SuppressWarnings("unused")
  private int fallback(Object value) {
    Class<?> receiverClass = value.getClass();
    int index = classValue.get(receiverClass);
    
    if (depth == MAX_DEPTH) {
      setTarget(GET.bindTo(classValue));
      return index;
    }
    
    setTarget(guardWithTest(TYPECHECK.bindTo(receiverClass),
        dropArguments(constant(int.class, index), 0, Object.class),
        new TypeSwitchCallSite(depth + 1, callsite, classValue).dynamicInvoker()));
    return index;
  }
  
  @SuppressWarnings("unused")
  private static boolean typecheck(Class<?> type, Object value) {
    return value.getClass() == type;
  }
  
  @SuppressWarnings("unused")
  private static int get(ClassValue<Integer> classValue, Object value) {
    return classValue.get(value.getClass());
  } 
  
  static MethodHandle wrapNullIfNecessary(boolean nullMatch, MethodHandle mh) {
    if (!nullMatch) {
      return mh;
    }
    return guardWithTest(NULLCHECK,
        dropArguments(constant(int.class, TypeSwitch.NULL_MATCH), 0, Object.class),
        mh);
  }
}