package com.github.forax.exotic;

import static com.github.forax.exotic.StringSwitch.NO_MATCH;
import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.util.HashMap;
import java.util.Objects;

class StringSwitchCallSite extends MutableCallSite {
  private static final MethodType STRING_TO_INT = methodType(int.class, String.class);
  private static final MethodHandle FALLBACK, EQUALS, GET_OR_DEFAULT, NULLCHECK;
  static {
    Lookup lookup = MethodHandles.lookup();
    try {
      FALLBACK = lookup.findVirtual(StringSwitchCallSite.class, "fallback", STRING_TO_INT);
      EQUALS = lookup.findVirtual(String.class, "equals", methodType(boolean.class, Object.class));
      MethodHandle get = lookup.findVirtual(HashMap.class, "getOrDefault", methodType(Object.class, Object.class, Object.class));
      GET_OR_DEFAULT = MethodHandles.insertArguments(get, 2, -1).asType(methodType(int.class, HashMap.class, String.class));
      MethodHandle nullCheck = lookup.findStatic(Objects.class, "isNull", methodType(boolean.class, Object.class));
      NULLCHECK = nullCheck.asType(methodType(boolean.class, String.class));
    } catch(NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }
  
  private static final int MAX_DEPTH = 32;
  
  private final int depth;
  private final StringSwitchCallSite callsite;
  private final String[] stringcases;
  private final HashMap<String, Integer> map;
  
  private StringSwitchCallSite(String[] stringcases, HashMap<String, Integer> map) {
    super(STRING_TO_INT);
    this.depth = 0;
    this.callsite = this;
    this.stringcases = stringcases;
    this.map = map;
    setTarget(FALLBACK.bindTo(this));
  }
  
  private StringSwitchCallSite(int depth, StringSwitchCallSite callsite, String[] stringcases, HashMap<String, Integer> map) {
    super(STRING_TO_INT);
    this.depth = depth;
    this.callsite = callsite;
    this.stringcases = stringcases;
    this.map = map;
    setTarget(FALLBACK.bindTo(this));
  }

  static StringSwitchCallSite create(String[] stringcases) {
    HashMap<String, Integer> map = new HashMap<>();  
    for(int i = 0; i < stringcases.length; i++) {
      String stringcase = Objects.requireNonNull(stringcases[i]);
      if (map.put(stringcase, i) != null) {
        throw new IllegalStateException(stringcase + " value appear more than once");
      }
    }
    return new StringSwitchCallSite(stringcases, map);
  }
  
  @SuppressWarnings("unused")
  private int fallback(String value) {
    Objects.requireNonNull(value);
    int index = map.getOrDefault(value, NO_MATCH);
    
    //System.out.println("depth " + depth);
    
    if (depth == MAX_DEPTH) {
      //System.out.println("reach max depth");
      callsite.setTarget(GET_OR_DEFAULT.bindTo(map));
      return index;
    }
    
    if (depth == stringcases.length) {
      //System.out.println("reach cases length");
      callsite.setTarget(createCascadeIfEquals(stringcases));
      return index;
    }
    
    setTarget(guardWithTest(insertArguments(EQUALS, 1, value),
        dropArguments(constant(int.class, index), 0, String.class),
        new StringSwitchCallSite(depth + 1, callsite, stringcases, map).dynamicInvoker()));
    return index;
  }
  
  private static MethodHandle createCascadeIfEquals(String[] stringcases) {
    MethodHandle target = dropArguments(constant(int.class, NO_MATCH), 0, String.class);
    for(int i = stringcases.length; --i >= 0;) {
      String stringcase = stringcases[i];
      target = guardWithTest(insertArguments(EQUALS, 1, stringcase),
          dropArguments(constant(int.class, i), 0, String.class),
          target);
    }
    return target;
  }
  
  static MethodHandle wrapNullIfNecessary(boolean nullMatch, MethodHandle mh) {
    if (!nullMatch) {
      return mh;
    }
    return guardWithTest(NULLCHECK,
        dropArguments(constant(int.class, StringSwitch.NULL_MATCH), 0, String.class),
        mh);
  }
}