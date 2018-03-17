package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Collections.nCopies;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.util.ArrayDeque;
import java.util.Objects;

interface StructuralCallImpl extends StructuralCall {
  @Override
  @SuppressWarnings("unchecked")
  default <R> R invoke(Object receiver) {
    return (R) call(1, receiver, null, null, null, null, null, null, null, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <R> R invoke(Object receiver, Object arg1) {
    return (R) call(2, receiver, arg1, null, null, null, null, null, null, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <R> R invoke(Object receiver, Object arg1, Object arg2) {
    return (R) call(3, receiver, arg1, arg2, null, null, null, null, null, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <R> R invoke(Object receiver, Object arg1, Object arg2, Object arg3) {
    return (R) call(4, receiver, arg1, arg2, arg3, null, null, null, null, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <R> R invoke(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) {
    return (R) call(5, receiver, arg1, arg2, arg3, arg4, null, null, null, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <R> R invoke(
      Object receiver, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    return (R) call(6, receiver, arg1, arg2, arg3, arg4, arg5, null, null, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <R> R invoke(
      Object receiver,
      Object arg1,
      Object arg2,
      Object arg3,
      Object arg4,
      Object arg5,
      Object arg6) {
    return (R) call(7, receiver, arg1, arg2, arg3, arg4, arg5, arg6, null, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <R> R invoke(
      Object receiver,
      Object arg1,
      Object arg2,
      Object arg3,
      Object arg4,
      Object arg5,
      Object arg6,
      Object arg7) {
    return (R) call(8, receiver, arg1, arg2, arg3, arg4, arg5, arg6, arg7, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <R> R invoke(
      Object receiver,
      Object arg1,
      Object arg2,
      Object arg3,
      Object arg4,
      Object arg5,
      Object arg6,
      Object arg7,
      Object arg8) {
    return (R) call(9, receiver, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  Object call(
      int argCount,
      Object receiver,
      Object arg1,
      Object arg2,
      Object arg3,
      Object arg4,
      Object arg5,
      Object arg6,
      Object arg7,
      Object arg8);

  static MethodHandle findMethodHandle(Lookup lookup, String name, MethodType type) {
    Objects.requireNonNull(lookup);
    Objects.requireNonNull(name);
    Objects.requireNonNull(type);
    MethodHandle mh =
        new InliningCacheCallSite(type.insertParameterTypes(0, Object.class), lookup, name)
            .dynamicInvoker();
    int parameterCount = mh.type().parameterCount();
    if (parameterCount != 9) {
      mh = dropArguments(mh, parameterCount, nCopies(9 - parameterCount, Object.class));
    }
    mh = mh.asType(genericMethodType(9));

    // check that parameterCount == argCount
    MethodHandle guard =
        guardWithTest(
            insertArguments(InliningCacheCallSite.COUNTCHECK, 0, parameterCount),
            dropArguments(mh, 0, int.class),
            dropArguments(InliningCacheCallSite.ERRORCOUNT, 1, mh.type().parameterList()));
    return guard;
  }

  class InliningCacheCallSite extends MutableCallSite {
    private static final MethodHandle FALLBACK, TYPECHECK;
    static final MethodHandle COUNTCHECK, ERRORCOUNT;

    static {
      Lookup lookup = MethodHandles.lookup();
      try {
        FALLBACK =
            lookup.findVirtual(
                InliningCacheCallSite.class,
                "fallback",
                methodType(MethodHandle.class, Object.class));
        TYPECHECK =
            lookup.findStatic(
                InliningCacheCallSite.class,
                "typecheck",
                methodType(boolean.class, Class.class, Object.class));
        COUNTCHECK =
            lookup.findStatic(
                InliningCacheCallSite.class,
                "countcheck",
                methodType(boolean.class, int.class, int.class));
        ERRORCOUNT =
            lookup.findStatic(
                InliningCacheCallSite.class, "errorcount", methodType(Object.class, int.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }

    private final Lookup lookup;
    private final String name;

    InliningCacheCallSite(MethodType type, Lookup lookup, String name) {
      super(type);
      this.lookup = lookup;
      this.name = name;
      setTarget(
          MethodHandles.foldArguments(MethodHandles.exactInvoker(type), FALLBACK.bindTo(this)));
    }

    @SuppressWarnings("unused")
    private MethodHandle fallback(Object receiver) {
      Class<?> receiverClass = receiver.getClass();
      MethodHandle target;
      try {
        target = findTarget(lookup, receiverClass, name, type().dropParameterTypes(0, 1));
      } catch (NoSuchMethodException e) {
        throw (NoSuchMethodError) new NoSuchMethodError().initCause(e);
      } catch (IllegalAccessException e) {
        throw (IllegalAccessError) new IllegalAccessError().initCause(e);
      }

      target = target.asType(type());
      MethodHandle guard =
          MethodHandles.guardWithTest(
              TYPECHECK.bindTo(receiverClass),
              target,
              new InliningCacheCallSite(type(), lookup, name).dynamicInvoker());
      setTarget(guard);
      return target;
    }

    private static MethodHandle findTarget(
        Lookup lookup, Class<?> receiverClass, String name, MethodType parameterType)
        throws IllegalAccessException, NoSuchMethodException {
      ArrayDeque<Class<?>> queue = new ArrayDeque<>();
      queue.add(receiverClass);

      Class<?> type;
      IllegalAccessException illegalAccess = null;
      while ((type = queue.poll()) != null) {
        try {
          return lookup.findVirtual(type, name, parameterType);
        } catch (NoSuchMethodException e) {
          if (illegalAccess == null) {
            throw e;
          }
        } catch (IllegalAccessException e) {
          if (illegalAccess == null) {
            illegalAccess = e;
          }

          // try super types
          queue.add(type.getSuperclass());
          for (Class<?> interfaze : type.getInterfaces()) {
            queue.add(interfaze);
          }
        }
      }
      assert illegalAccess != null;
      throw illegalAccess;
    }

    @SuppressWarnings("unused")
    private static boolean typecheck(Class<?> type, Object o) {
      return o.getClass() == type;
    }

    @SuppressWarnings("unused")
    private static boolean countcheck(int parameterCount, int argumentCount) {
      return parameterCount == argumentCount;
    }

    @SuppressWarnings("unused")
    private static Object errorcount(int argumentCount) {
      throw new IllegalArgumentException("wrong number of argument " + argumentCount);
    }
  }
}
