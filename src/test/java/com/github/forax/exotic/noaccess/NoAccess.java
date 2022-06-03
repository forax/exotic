package com.github.forax.exotic.noaccess;

import com.github.forax.exotic.StructuralCallTests;

/**
 * A public class with a private method.
 *
 * @see StructuralCallTests#cannotAccessToAPrivateMethod()
 */
public class NoAccess {
    @SuppressWarnings("unused")
    private String m(String s) {
      return s;
    }
}