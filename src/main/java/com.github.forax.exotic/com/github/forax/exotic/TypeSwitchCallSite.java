package com.github.forax.exotic;

import static com.github.forax.exotic.TypeSwitch.NO_MATCH;
import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.ref.WeakReference;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;

class TypeSwitchCallSite extends MutableCallSite {
  private interface Strategy {
    int index(Class<?> receiverClass);
    MethodHandle target();
    
    static Strategy isInstance(Class<?>[] typecases) {
      WeakReference<Class<?>>[] refs = createRefArray(typecases);
      return new Strategy() {
        @Override
        public int index(Class<?> receiverClass) {
          for(int i = 0; i < refs.length; i++) {
            Class<?> typecase = refs[i].get();
            if (typecase != null && typecase.isAssignableFrom(receiverClass)) {
              return i;
            }
          }
          return NO_MATCH;
        }
        @Override
        public MethodHandle target() {
          MethodHandle mh = dropArguments(constant(int.class, NO_MATCH), 0, Object.class);  
          for(int i = refs.length; --i >= 0;) {
            Class<?> typecase = refs[i].get();
            if (typecase == null) {
              continue;
            }
            mh = guardWithTest(IS_INSTANCE.bindTo(typecase),
                dropArguments(constant(int.class, i), 0, Object.class),
                mh);
          }
          return mh;
        }
      };
    }
    
    static Strategy classValue(Class<?>[] typecases) {
      ClassValue<Integer> classValue = createClassValue(typecases);
      return new Strategy() {
        @Override
        public int index(Class<?> receiverClass) {
          return classValue.get(receiverClass);
        }
        @Override
        public MethodHandle target() {
          return GET.bindTo(classValue);
        }
      };
    }
    
    private static WeakReference<Class<?>>[] createRefArray(Class<?>[] typecases) {
      @SuppressWarnings("unchecked")
      WeakReference<Class<?>>[] refs = (WeakReference<Class<?>>[])new WeakReference<?>[typecases.length];
      for(int i = 0; i < typecases.length; i++) {
        refs[i] = new WeakReference<>(typecases[i]);
      }
      return refs;
    }
    
    private static ClassValue<Integer> createClassValue(Class<?>[] typecases) {
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
          int index = NO_MATCH;
          Class<?> superclass = type.getSuperclass();
          if (superclass != null) {
            index = get(superclass);
          }
          for(Class<?> supertype: type.getInterfaces()) {
            int localIndex = get(supertype);
            if (localIndex != NO_MATCH) {
              index = (index == NO_MATCH)? localIndex: Math.min(index, localIndex);
            }
          }
          return index;
        }
      };
      for(int i = 0; i < typecases.length; i++) {
        local.set(i);  // inject value
        classValue.get(typecases[i]);
      }
      local.set(null);  // no injection anymore
      return classValue;
    }
  }
  
  private static final MethodType OBJECT_TO_INT = methodType(int.class, Object.class);
  private static final MethodHandle FALLBACK, TYPECHECK, NULLCHECK;
  static final MethodHandle GET, IS_INSTANCE;
  static {
    Lookup lookup = MethodHandles.lookup();
    try {
      FALLBACK = lookup.findVirtual(TypeSwitchCallSite.class, "fallback", OBJECT_TO_INT);
      TYPECHECK = lookup.findStatic(TypeSwitchCallSite.class, "typecheck", methodType(boolean.class, Class.class, Object.class));
      GET = lookup.findStatic(TypeSwitchCallSite.class, "get", methodType(int.class, ClassValue.class, Object.class));
      NULLCHECK = lookup.findStatic(Objects.class, "isNull", methodType(boolean.class, Object.class));
      IS_INSTANCE = lookup.findVirtual(Class.class, "isInstance", methodType(boolean.class, Object.class));
    } catch(NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }
  
  private static final int MAX_DEPTH = 8;
  private static final int STRATEGY_CUT_OFF = 5;
  
  private final int depth;
  private final TypeSwitchCallSite callsite;
  private final Strategy strategy;
  
  private TypeSwitchCallSite(Strategy strategy) {
    super(OBJECT_TO_INT);
    this.depth = 0;
    this.callsite = this;
    this.strategy = strategy;
    setTarget(FALLBACK.bindTo(this));
  }
  
  private TypeSwitchCallSite(int depth, TypeSwitchCallSite callsite, Strategy strategy) {
    super(OBJECT_TO_INT);
    this.depth = depth;
    this.callsite = callsite;
    this.strategy = strategy;
    setTarget(FALLBACK.bindTo(this));
  }

  static TypeSwitchCallSite create(Class<?>[] typecases) {
    for(Class<?> typecase: typecases) {
      Objects.requireNonNull(typecase);
    }
    
    Strategy strategy = (typecases.length < STRATEGY_CUT_OFF)?
      Strategy.isInstance(typecases): Strategy.classValue(typecases);
    return new TypeSwitchCallSite(strategy);
  }
  
  @SuppressWarnings("unused")
  private int fallback(Object value) {
    Class<?> receiverClass = value.getClass();
    int index = strategy.index(receiverClass);
    
    if (depth == MAX_DEPTH) {
      setTarget(strategy.target());
      return index;
    }
    
    setTarget(guardWithTest(TYPECHECK.bindTo(receiverClass),
        dropArguments(constant(int.class, index), 0, Object.class),
        new TypeSwitchCallSite(depth + 1, callsite, strategy).dynamicInvoker()));
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