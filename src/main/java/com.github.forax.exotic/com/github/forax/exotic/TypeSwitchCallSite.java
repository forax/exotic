package com.github.forax.exotic;

import static com.github.forax.exotic.TypeSwitch.NO_MATCH;
import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.ref.WeakReference;
//import java.lang.reflect.Field;
//import java.util.Base64;
import java.util.Objects;
//import java.util.function.Predicate;

//import sun.misc.Unsafe;

class TypeSwitchCallSite extends MutableCallSite {
  private interface Strategy {
    int index(Class<?> receiverClass);
    MethodHandle target(/*Lookup lookup*/);
    
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
        public MethodHandle target(/*Lookup lookup*/) {
          MethodHandle mh = dropArguments(constant(int.class, NO_MATCH), 0, Object.class);  
          for(int i = refs.length; --i >= 0;) {
            Class<?> typecase = refs[i].get();
            if (typecase == null) {
              continue;
            }
            mh = guardWithTest(IS_INSTANCE.bindTo(typecase)/*PREDICATE_TEST.bindTo(createPredicateFromSpecies(lookup, typecase))*/,
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
        public MethodHandle target(/*Lookup lookup*/) {
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
  
  /*public static class IsInstanceSpecies implements Predicate<Object> {
    @Override
    public boolean test(Object o) {
      return o instanceof String;
    }
    
    public static Predicate<Object> create() {
      return new IsInstanceSpecies();
    }
  }
  
  static {
    String name = '/' + IsInstanceSpecies.class.getName().replace('.', '/')+".class";
    System.out.println(name);
    java.io.InputStream input = IsInstanceSpecies.class.getResourceAsStream(name);
    byte[] bytecode;
    try {
      bytecode = input.readAllBytes();
    } catch (java.io.IOException e) {
      throw new AssertionError(e);
    }
    String text = Base64.getEncoder().encodeToString(bytecode);
    System.out.println(text);
  }*/
  
  /*private static final byte[] ISINSTANCE_SPECIES_BYTECODE;
  private static final Unsafe UNSAFE;
  static {
    String data = "yv66vgAAADUAIgcAAgEAPGNvbS9naXRodWIvZm9yYXgvZXhvdGljL1R5cGVTd2l0Y2hDYWxsU2l0ZSRJc0luc3RhbmNlU3BlY2llcwcABAEAEGphdmEvbGFuZy9PYmplY3QHAAYBABxqYXZhL3V0aWwvZnVuY3Rpb24vUHJlZGljYXRlAQAGPGluaXQ+AQADKClWAQAEQ29kZQoAAwALDAAHAAgBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQA+TGNvbS9naXRodWIvZm9yYXgvZXhvdGljL1R5cGVTd2l0Y2hDYWxsU2l0ZSRJc0luc3RhbmNlU3BlY2llczsBAAR0ZXN0AQAVKExqYXZhL2xhbmcvT2JqZWN0OylaBwATAQAQamF2YS9sYW5nL1N0cmluZwEAAW8BABJMamF2YS9sYW5nL09iamVjdDsBAAZjcmVhdGUBACAoKUxqYXZhL3V0aWwvZnVuY3Rpb24vUHJlZGljYXRlOwEACVNpZ25hdHVyZQEANCgpTGphdmEvdXRpbC9mdW5jdGlvbi9QcmVkaWNhdGU8TGphdmEvbGFuZy9PYmplY3Q7PjsKAAEACwEAClNvdXJjZUZpbGUBABdUeXBlU3dpdGNoQ2FsbFNpdGUuamF2YQEARExqYXZhL2xhbmcvT2JqZWN0O0xqYXZhL3V0aWwvZnVuY3Rpb24vUHJlZGljYXRlPExqYXZhL2xhbmcvT2JqZWN0Oz47AQAMSW5uZXJDbGFzc2VzBwAgAQAqY29tL2dpdGh1Yi9mb3JheC9leG90aWMvVHlwZVN3aXRjaENhbGxTaXRlAQARSXNJbnN0YW5jZVNwZWNpZXMAIQABAAMAAQAFAAAAAwABAAcACAABAAkAAAAvAAEAAQAAAAUqtwAKsQAAAAIADAAAAAYAAQAAAHMADQAAAAwAAQAAAAUADgAPAAAAAQAQABEAAQAJAAAAOQABAAIAAAAFK8EAEqwAAAACAAwAAAAGAAEAAAB2AA0AAAAWAAIAAAAFAA4ADwAAAAAABQAUABUAAQAJABYAFwACABgAAAACABkACQAAACgAAgAAAAAACLsAAVm3ABqwAAAAAgAMAAAABgABAAAAegANAAAAAgAAAAMAGwAAAAIAHAAYAAAAAgAdAB4AAAAKAAEAAQAfACEACQ==";
    ISINSTANCE_SPECIES_BYTECODE = Base64.getDecoder().decode(data);
    
    try {
      Field field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      UNSAFE = (Unsafe)field.get(null);
    } catch (NoSuchFieldException | IllegalAccessException | IllegalStateException e) {
      throw new AssertionError(e);
    }
  }
  
  static Predicate<?> createPredicateFromSpecies(Lookup lookup, Class<?> clazz) {
    MethodHandle mh = predicateFromSpecies(lookup, clazz);
    try {
      return (Predicate<?>)mh.invokeExact();
    } catch(RuntimeException | Error e) {
      throw e;
    } catch (Throwable e) {
      throw new AssertionError(e);
    }
  }
  
  private static MethodHandle predicateFromSpecies(Lookup lookup, Class<?> clazz) {
    Class<?> lookupClass = lookup.lookupClass();
    
    Object[] patches = new Object[19];
    patches[18] = clazz.getName().replace('.', '/');
    
    Class<?> anonymousClass = UNSAFE.defineAnonymousClass(lookupClass, ISINSTANCE_SPECIES_BYTECODE, patches);
    try {
      return lookup.findStatic(anonymousClass, "create", methodType(Predicate.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }*/
  
  private static final MethodType OBJECT_TO_INT = methodType(int.class, Object.class);
  private static final MethodHandle FALLBACK, TYPECHECK, NULLCHECK;
  static final MethodHandle GET, IS_INSTANCE/*, PREDICATE_TEST*/;
  static {
    Lookup lookup = MethodHandles.lookup();
    try {
      FALLBACK = lookup.findVirtual(TypeSwitchCallSite.class, "fallback", OBJECT_TO_INT);
      TYPECHECK = lookup.findStatic(TypeSwitchCallSite.class, "typecheck", methodType(boolean.class, Class.class, Object.class));
      GET = lookup.findStatic(TypeSwitchCallSite.class, "get", methodType(int.class, ClassValue.class, Object.class));
      NULLCHECK = lookup.findStatic(Objects.class, "isNull", methodType(boolean.class, Object.class));
      IS_INSTANCE = lookup.findVirtual(Class.class, "isInstance", methodType(boolean.class, Object.class));
      //PREDICATE_TEST = lookup.findVirtual(Predicate.class, "test", methodType(boolean.class, Object.class));
    } catch(NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }
  
  private static final int MAX_DEPTH = 8;
  private static final int STRATEGY_CUT_OFF = 5;
  
  //private final Lookup lookup;
  private final int depth;
  private final TypeSwitchCallSite callsite;
  private final Strategy strategy;
  
  private TypeSwitchCallSite(/*Lookup lookup,*/ Strategy strategy) {
    super(OBJECT_TO_INT);
    this.depth = 0;
    this.callsite = this;
    //this.lookup = lookup;
    this.strategy = strategy;
    setTarget(FALLBACK.bindTo(this));
  }
  
  private TypeSwitchCallSite(int depth, TypeSwitchCallSite callsite,/* Lookup lookup,*/ Strategy strategy) {
    super(OBJECT_TO_INT);
    this.depth = depth;
    this.callsite = callsite;
    //this.lookup = lookup;
    this.strategy = strategy;
    setTarget(FALLBACK.bindTo(this));
  }

  static TypeSwitchCallSite create(/*Lookup lookup,*/ Class<?>[] typecases) {
    for(Class<?> typecase: typecases) {
      Objects.requireNonNull(typecase);
    }
    
    Strategy strategy = (typecases.length < STRATEGY_CUT_OFF)?
      Strategy.isInstance(typecases): Strategy.classValue(typecases);
    return new TypeSwitchCallSite(/*lookup,*/ strategy);
  }
  
  @SuppressWarnings("unused")
  private int fallback(Object value) {
    Class<?> receiverClass = value.getClass();
    int index = strategy.index(receiverClass);
    
    if (depth == MAX_DEPTH) {
      setTarget(strategy.target(/*lookup*/));
      return index;
    }
    
    setTarget(guardWithTest(TYPECHECK.bindTo(receiverClass),
        dropArguments(constant(int.class, index), 0, Object.class),
        new TypeSwitchCallSite(depth + 1, callsite, /*lookup,*/ strategy).dynamicInvoker()));
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