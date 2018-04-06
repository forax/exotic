package com.github.forax.exotic;

import java.lang.invoke.MethodHandle;

@FunctionalInterface
public interface TypeSwitch {
  int typeSwitch(Object value);
  
  int BAD_MATCH = -2;
  int NULL_MATCH = -1;
  
  public static TypeSwitch create(boolean nullMatch, Class<?>... typecases) {
    MethodHandle mh = TypeSwitchCallSite.wrapNullIfNecessary(nullMatch, TypeSwitchCallSite.create(typecases).dynamicInvoker());
    return value -> {
      try {
        return (int)mh.invokeExact(value);  
      } catch(Throwable t) {
        throw Thrower.rethrow(t);
      }
    };
  }
}
