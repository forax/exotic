package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.ToIntFunction;

import org.junit.jupiter.api.Test;

import com.github.forax.exotic.StableField;

@SuppressWarnings("static-method")
class StableFieldExampleTests {
  static class SystemInfo {
    static final ToIntFunction<SystemInfo> CPU_COUNT = StableField.intGetter(lookup(), SystemInfo.class, "cpuCount");
    static final SystemInfo INSTANCE = new SystemInfo();
    
    private SystemInfo() {
      // enforce singleton
    }
    
    int cpuCount;  // stable

    public int getCpuCount() {
      int cpuCount = CPU_COUNT.applyAsInt(this);
      if (cpuCount == 0) {
        return this.cpuCount = Runtime.getRuntime().availableProcessors();
      }
      return cpuCount;
    }
  }

  @Test
  void test() {
    assertTrue(SystemInfo.INSTANCE.getCpuCount() > 0);
  }
}
