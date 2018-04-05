package com.github.forax.exotic;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

@FunctionalInterface
public interface TypeSwitch {
  int typeSwitch(Object value);
  
  int BAD_MATCH = -2;
  int NULL_MATCH = -1;
  
  private static ClassValue<Integer> createClassValue(Class<?>... cases) {
    ThreadLocal<Integer> local = new ThreadLocal<>();
    ClassValue<Integer> classValue = new ClassValue<>() {
      @Override
      protected Integer computeValue(Class<?> type) {
        Integer index = local.get();
        if (index != null) {  // injection
          return index;
        }
        return computeFromSupertypes(type);
      }

      private Integer computeFromSupertypes(Class<?> type) {
        int index = BAD_MATCH;
        Class<?> superclass = type.getSuperclass();
        if (superclass != null) {
          index = get(superclass);
        }
        for(Class<?> supertype: type.getInterfaces()) {
          int localIndex = get(supertype);
          if (localIndex != BAD_MATCH) {
            index = (index == BAD_MATCH)? localIndex: Math.min(index, localIndex);
          }
        }
        return index;
      }
    };
    for(int i = 0; i < cases.length; i++) {
      local.set(i);  // inject value
      classValue.get(Objects.requireNonNull(cases[i]));
    }
    local.set(null);  // no injection anymore
    return classValue;
  }
  
  public static TypeSwitch create(boolean nullMatch, Class<?>... cases) {
    ClassValue<Integer> classValue = createClassValue(cases);
    MethodHandle mh = TypeSwitchCallSite.wrapNullIfNecessary(nullMatch, new TypeSwitchCallSite(classValue).dynamicInvoker());
    return value -> {
      try {
        return (int)mh.invokeExact(value);  
      } catch(Throwable t) {
        throw Thrower.rethrow(t);
      }
    };
  }
}
