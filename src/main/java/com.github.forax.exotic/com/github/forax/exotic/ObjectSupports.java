package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.filterReturnValue;
import static java.lang.invoke.MethodHandles.foldArguments;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import com.github.forax.exotic.ObjectSupport.ProjectionFunction;

class ObjectSupports {
  private static final MethodHandle OBJECT_SUPPORT_FACTORY;
  static {
    Object unsafe;
    Method defineAnonymousClass;
    try {
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      defineAnonymousClass = unsafeClass.getMethod("defineAnonymousClass", Class.class, byte[].class, Object[].class);
      unsafe = unsafeField.get(null);
    } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
    
    byte[] data ;
    try(InputStream input = ObjectSupportImpl.class.getResourceAsStream("/" + ObjectSupportImpl.class.getName().replace('.', '/') + ".class")) {
      data = readAllBytes(input);
    } catch(IOException e) {
      throw new AssertionError(e);
    }
    
    try {
      Class<?> impl = (Class<?>)defineAnonymousClass.invoke(unsafe, ObjectSupport.class, data, null);
      OBJECT_SUPPORT_FACTORY = lookup().findStatic(impl, "create", methodType(ObjectSupport.class, MethodHandle.class, MethodHandle.class));
    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }
  
  private static byte[] readAllBytes(InputStream input) throws IOException {
    byte[] buffer = new byte[8192];
    int read;
    int total = 0;
    while((read = input.read(buffer, total, buffer.length - total)) != -1) {
      total += read;
      if (read == 0) {
        buffer = Arrays.copyOf(buffer, buffer.length + 8192);
      }
    }
    return Arrays.copyOf(buffer, total);
  }
  
  static <T> ObjectSupport<T> createUsingFieldNames(Lookup lookup, Class<T> type, String... fieldNames) {
    requireNonNull(lookup);
    requireNonNull(type);
    requireNonNull(fieldNames);
    return create(lookup, findFields(type, fieldNames));
  }
  
  static <T> ObjectSupport<T> createUsingReflectFields(Lookup lookup, Class<T> type, Function<? super Class<T>, ? extends Field[]> transformer) {
    requireNonNull(lookup);
    requireNonNull(type);
    requireNonNull(transformer);
    return create(lookup, Arrays.stream(transformer.apply(type)).filter(f -> !isStatic(f.getModifiers())).toArray(Field[]::new));
  }
  
  static <T> ObjectSupport<T> createUsingLambdas(Lookup lookup, Class<T> type, ProjectionFunction<? super T, ?>[] projections) {
    requireNonNull(lookup);
    requireNonNull(type);
    requireNonNull(projections);
    String[] fieldNames = ObjectSupportLambdas.extractFieldNames(lookup, projections);
    return create(lookup, findFields(type, fieldNames));
  }
  
  private static <T> ObjectSupport<T> create(Lookup lookup, Field[] fields) {
    MethodHandle equalsMH = createEqualsMH(lookup, fields);
    MethodHandle hashCodeMH = createHashCodeMH(lookup, fields);
    try {
      return (ObjectSupport<T>) OBJECT_SUPPORT_FACTORY.invokeExact(equalsMH, hashCodeMH);
    } catch (Throwable e) {
      throw Thrower.rethrow(e);
    }
  }
  
  private static final class ObjectSupportImpl implements ObjectSupport<Object> {
    private final MethodHandle equalsMH;
    private final MethodHandle hashCodeMH;
    
    private ObjectSupportImpl(MethodHandle equalsMH, MethodHandle hashCodeMH) {
      this.equalsMH = equalsMH;
      this.hashCodeMH = hashCodeMH;
    }
    
    @SuppressWarnings("unused")
    public static ObjectSupport<Object> create(MethodHandle equalsMH, MethodHandle hashCodeMH) {
      return new ObjectSupportImpl(equalsMH, hashCodeMH);
    }

    @Override
    public boolean equals(Object self, Object other) {
      try {
        return (boolean) equalsMH.invokeExact(self, other);
      } catch (Throwable e) {
        throw Thrower.rethrow(e);
      }
    }
    
    @Override
    public int hashCode(Object self) {
      try {
        return (int) hashCodeMH.invokeExact(self);
      } catch (Throwable e) {
        throw Thrower.rethrow(e);
      }
    }
  }
  
  /*
  public static ToStringer toStringer(Lookup lookup, String... fieldNames) {
    Class<?> lookupClass = lookup.lookupClass();
    requireFinalClass(lookupClass);
    requireNoInheritance(lookupClass);
    
    MethodHandle mh = createToStringMH(lookup, asFields(lookupClass, fieldNames));
    return self -> {
      try {
        return (String) mh.invokeExact(self);
      } catch (Throwable e) {
        throw Thrower.rethrow(e);
      }
    };
  }*/
  

  private static MethodHandle createEqualsMH(Lookup lookup, Field[] fields) {
    // move primitives at the end of the array, so they will be tested first
    // (createEquals creates equals tests from the last to the first)
    Integer[] orders = new Integer[fields.length];
    for(int i = 0; i < orders.length; i++) {
      orders[i] = i; 
    }
    
    Arrays.sort(orders, (index1, index2) -> {
      Class<?> t1 = fields[index1].getType();
      Class<?> t2 = fields[index2].getType();
      if (t1.isPrimitive()) {
        if (!t2.isPrimitive()) {
          return 1;
        }
      } else {
        if (t2.isPrimitive()) {
          return -1;
        }
      }
      // for both references and primitives, move them in the array so the first in
      // fields is the last in sortedFields
      return Integer.compare(index2, index1);
    });
    
    Field[] sortedFields = new Field[fields.length];
    for(int i = 0; i < sortedFields.length; i++) {
      sortedFields[i] = fields[orders[i]]; 
    }
    return EqualsUtils.createEquals(lookup, asGetters(lookup, sortedFields));
  }
  
  private static MethodHandle createHashCodeMH(Lookup lookup, Field[] fields) {
    return HashCodeUtils.createHashCode(lookup, lookup.lookupClass(), asGetters(lookup, fields))
        .asType(methodType(int.class, Object.class));
  }
  
  /*
  private static MethodHandle createToStringMH(Lookup lookup, Field[] fields) {
    int length = fields.length;
    StringBuilder format = new StringBuilder();
    String separator = "";
    Class<?>[] parameterTypes = new Class<?>[length];
    MethodHandle[] getters = new MethodHandle[length];
    for (int i = 0; i < length; i++) {
      Field field = fields[i];
      format.append(separator).append(field.getName()).append("=\1");
      separator = " ";

      MethodHandle getter;
      try {
        getter = lookup.unreflectGetter(field);
      } catch (IllegalAccessException e) {
        throw newLinkageError(e);
      }
      Class<?> type = field.getType();
      if (type.isArray()) {
        MethodHandle adapter = ArrayToStringUtils.adapter(type).asType(methodType(String.class, type));
        getter = filterReturnValue(getter, adapter);
        type = String.class;
      }
      getters[i] = getter;
      parameterTypes[i] = type;
    }

    // ask for a MethodHandle that will do the concatenation
    MethodHandle target;
    try {
      target = makeConcatWithConstants(lookup, "toString", methodType(String.class, parameterTypes), format.toString())
          .dynamicInvoker();
    } catch (StringConcatException e) {
      throw newLinkageError(e);
    }

    // apply all getters
    target = filterArguments(target, 0, getters);

    // duplicate the first argument (this)
    target = permuteArguments(target, methodType(String.class, lookup.lookupClass()), new int[length]);

    return target.asType(methodType(String.class, Object.class));
  }
  */
  
  private static Field[] findFields(Class<?> type, String[] names) {
    Field[] fields = new Field[names.length];
    for(int i = 0; i < fields.length; i++) {
      String name = names[i];
      try {
        fields[i] = type.getDeclaredField(name);
      } catch (NoSuchFieldException e) {
        throw new IllegalStateException("no field " + name + " found", e);
      }
    }
    return fields;
  }
  
  private static MethodHandle asGetter(Lookup lookup, Field field) {
    try {
      return lookup.unreflectGetter(field);
    } catch (IllegalAccessException e) {
      throw newLinkageError(e);
    }
  }
  
  private static MethodHandle[] asGetters(Lookup lookup, Field[] fields) {
    MethodHandle[] mhs = new MethodHandle[fields.length];
    for(int i = 0; i < mhs.length; i++) {
      mhs[i] = asGetter(lookup, fields[i]);
    }
    return mhs;
  }

  private static final int PRIMITIVE_COUNT = 8;

  static int projection(Class<?> type) {
    switch (type.getName()) {
    case "boolean":
      return 0;
    case "byte":
      return 1;
    case "char":
      return 2;
    case "short":
      return 3;
    case "int":
      return 4;
    case "long":
      return 5;
    case "float":
      return 6;
    case "double":
      return 7;
    default:
      throw new AssertionError();
    }
  }

  static LinkageError newLinkageError(Throwable e) {
    return (LinkageError) new LinkageError().initCause(e);
  }

  /*
  private static class ArrayToStringUtils {
    // the code that access this array is racy but this is a cache, so that's not an issue
    private static final MethodHandle[] ADAPTER_CACHE = new MethodHandle[PRIMITIVE_COUNT + 1];
    private static final int OBJECT_INDEX = PRIMITIVE_COUNT;

    static MethodHandle adapter(Class<?> arrayType) {
      Class<?> componentType = arrayType.getComponentType();
      int index = componentType.isPrimitive() ? projection(componentType) : OBJECT_INDEX;
      MethodHandle mh = ADAPTER_CACHE[index];
      if (mh != null) {
        return mh;
      }
      Class<?> erasedType = componentType.isPrimitive() ? arrayType : Object[].class;
      try {
        mh = publicLookup().findStatic(Arrays.class, "toString", methodType(String.class, erasedType));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw newLinkageError(e);
      }

      MethodHandle concurrentMh = ADAPTER_CACHE[index];
      if (concurrentMh != null) {
        return concurrentMh;
      }
      ADAPTER_CACHE[index] = mh;
      return mh;
    }
  }*/

  private static class EqualsUtils {
    @SuppressWarnings("unused")
    private static boolean same(boolean b1, boolean b2) {
      return b1 == b2;
    }
    @SuppressWarnings("unused")
    private static boolean same(byte b1, byte b2) {
      return b1 == b2;
    }
    @SuppressWarnings("unused")
    private static boolean same(short s1, short s2) {
      return s1 == s2;
    }
    @SuppressWarnings("unused")
    private static boolean same(char c1, char c2) {
      return c1 == c2;
    }
    @SuppressWarnings("unused")
    private static boolean same(int i1, int i2) {
      return i1 == i2;
    }
    @SuppressWarnings("unused")
    private static boolean same(long l1, long l2) {
      return l1 == l2;
    }
    @SuppressWarnings("unused")
    private static boolean same(float f1, float f2) {
      return Float.floatToIntBits(f1) == Float.floatToIntBits(f2);
    }
    @SuppressWarnings("unused")
    private static boolean same(double d1, double d2) {
      return Double.doubleToLongBits(d1) == Double.doubleToLongBits(d2);
    }

    @SuppressWarnings("unused")
    private static boolean same(Object o1, Object o2) {
      return o1 == o2;
    }

    // the code that access this array is racy but this is a cache, so that's not an
    // issue
    private static final MethodHandle[] SAME_CACHE = new MethodHandle[PRIMITIVE_COUNT];

    private static MethodHandle primitiveEquals(Class<?> primitiveType) {
      int index = projection(primitiveType);
      MethodHandle mh = SAME_CACHE[index];
      if (mh != null) {
        return mh;
      }
      mh = findSameMH(primitiveType);
      MethodHandle concurrentMh = SAME_CACHE[index];
      if (concurrentMh != null) {
        return concurrentMh;
      }
      SAME_CACHE[index] = mh;
      return mh;
    }

    private static MethodHandle findSameMH(Class<?> type) {
      try {
        return lookup().findStatic(EqualsUtils.class, "same", methodType(boolean.class, type, type));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw newLinkageError(e);
      }
    }

    @SuppressWarnings("unused")
    private static boolean checkClass(Object o1, Object o2) {
      return o1.getClass() == o2.getClass();
    }
    
    private static final MethodHandle SAME_OBJECT, FIRST_NULL_CHECK, SECOND_NULL_CHECK, CLASS_CHECK, TRUE, FALSE;
    static {
      MethodHandle mh = findSameMH(Object.class);
      SAME_OBJECT = mh;
      FIRST_NULL_CHECK = dropArguments(insertArguments(mh, 1, (Object) null), 1, Object.class);
      SECOND_NULL_CHECK = dropArguments(insertArguments(mh, 1, (Object) null), 0, Object.class);
      TRUE = dropArguments(constant(boolean.class, true), 0, Object.class, Object.class);
      FALSE = dropArguments(constant(boolean.class, false), 0, Object.class, Object.class);
      try {
        CLASS_CHECK = lookup().findStatic(EqualsUtils.class, "checkClass", methodType(boolean.class, Object.class, Object.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }

    private static MethodHandle objectEquals(Lookup lookup, Class<?> type) {
      MethodHandle equals;
      try {
        equals = lookup.findVirtual(type, "equals", methodType(boolean.class, Object.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw newLinkageError(e);
      }

      // equivalent to (a == b)? true: (a == null)? false: a.equals(b)
      MethodType mType = methodType(boolean.class, type, type);
      return guardWithTest(SAME_OBJECT.asType(mType),
          TRUE.asType(mType),
          guardWithTest(FIRST_NULL_CHECK.asType(mType),
              FALSE.asType(mType),
              equals.asType(mType)));
    }

    private static MethodHandle equals(Lookup lookup, Class<?> type) {
      return type.isPrimitive() ? primitiveEquals(type) : objectEquals(lookup, type);
    }

    private static MethodHandle nullCheckAndClassCheck(MethodHandle target, Class<?> declaredType) {
      // equivalent to (b == null)? false: a.getClass() == b.getClass()? a.equals(b): false
      MethodType mType = methodType(boolean.class, declaredType, Object.class);
      return guardWithTest(
          SECOND_NULL_CHECK.asType(mType),
          FALSE.asType(mType),
          guardWithTest(
              CLASS_CHECK.asType(mType),
              target.asType(mType),
              FALSE.asType(mType)));
    }
    
    private static MethodHandle equalsAll(Lookup lookup, Class<?> declaredType, MethodHandle[] getters) {
      MethodHandle _false = FALSE.asType(methodType(boolean.class, declaredType, declaredType));

      MethodHandle target = TRUE.asType(methodType(boolean.class, declaredType, declaredType));
      for (MethodHandle getter : getters) {
        MethodHandle test = filterArguments(equals(lookup, getter.type().returnType()), 0, getter, getter);
        target = guardWithTest(test, target, _false);
      }
      return nullCheckAndClassCheck(target, declaredType);
    }
    
    static MethodHandle createEquals(Lookup lookup, MethodHandle[] getters) {
      return equalsAll(lookup, lookup.lookupClass(), getters).
          asType(methodType(boolean.class, Object.class, Object.class));
    }
  }

  

  private static class HashCodeUtils {
    // the code that access this array is racy but this is a cache, so that's not an
    // issue
    private static final MethodHandle[] HASH_CODE_CACHE = new MethodHandle[PRIMITIVE_COUNT];
    private static final Class<?>[] WRAPPERS = new Class<?>[] { Boolean.class, Byte.class, Character.class, Short.class,
        Integer.class, Long.class, Float.class, Double.class };

    private static Class<?> wrapper(Class<?> primitiveType) {
      return WRAPPERS[projection(primitiveType)];
    }

    private static MethodHandle primitiveHashCode(Class<?> primitiveType) {
      int index = projection(primitiveType);
      MethodHandle mh = HASH_CODE_CACHE[index];
      if (mh != null) {
        return mh;
      }
      try {
        mh = publicLookup().findStatic(wrapper(primitiveType), "hashCode", methodType(int.class, primitiveType));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw newLinkageError(e);
      }
      MethodHandle concurrentMh = HASH_CODE_CACHE[index];
      if (concurrentMh != null) {
        return concurrentMh;
      }
      HASH_CODE_CACHE[index] = mh;
      return mh;
    }

    private static final MethodHandle NULL_CHECK, REQUIRE_NON_NULL, ZERO, REDUCE;
    static {
      Lookup lookup = lookup();
      try {
        NULL_CHECK = lookup.findStatic(Objects.class, "isNull", methodType(boolean.class, Object.class));
        REQUIRE_NON_NULL = lookup.findStatic(Objects.class, "requireNonNull", methodType(Object.class, Object.class));
        REDUCE = lookup.findStatic(HashCodeUtils.class, "reduce", methodType(int.class, int.class, int.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw newLinkageError(e);
      }
      ZERO = dropArguments(constant(int.class, 0), 0, Object.class);
    }

    @SuppressWarnings("unused")
    private static int reduce(int value, int accumulator) {
      return value + accumulator * 31;
    }

    private static MethodHandle objectHashCode(Lookup lookup, Class<?> type) {
      MethodHandle hashCode;
      try {
        hashCode = lookup.findVirtual(type, "hashCode", methodType(int.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw newLinkageError(e);
      }
      return guardWithTest(NULL_CHECK.asType(methodType(boolean.class, type)),
          ZERO.asType(methodType(int.class, type)),
          hashCode);
    }

    private static MethodHandle hashCode(Lookup lookup, Class<?> type) {
      return type.isPrimitive() ? primitiveHashCode(type) : objectHashCode(lookup, type);
    }

    static MethodHandle createHashCode(Lookup lookup, Class<?> declaredType, MethodHandle[] getters) {
      MethodHandle target = dropArguments(constant(int.class, 1), 0, declaredType);
      if (getters.length == 0) {  // need to explicitly enforce that the parameter is not null
        return filterArguments(target, 0, REQUIRE_NON_NULL.asType(methodType(declaredType, declaredType)));
      }
      for (MethodHandle getter : getters) {
        MethodHandle hashField = filterReturnValue(getter, hashCode(lookup, getter.type().returnType()));
        target = foldArguments(
            foldArguments(
                dropArguments(REDUCE, 2, declaredType),
                dropArguments(hashField, 0, int.class)),
            target);
      }
      return target.asType(methodType(int.class, declaredType));
    }
  }
}
