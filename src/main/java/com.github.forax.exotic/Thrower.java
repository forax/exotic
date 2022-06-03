package com.github.forax.exotic;

interface Thrower<T extends Throwable> {
  RuntimeException magic(Throwable throwable) throws T;

  @SuppressWarnings("unchecked")
  static RuntimeException rethrow(Throwable t) {
    // works thanks to erasure and checked exception being a compiler thing, not a VM thing
    return ((Thrower<RuntimeException>)
            (Thrower<?>)
                (Thrower<Throwable>)
                    e -> {
                      throw e;
                    })
        .magic(t);
  }
}
