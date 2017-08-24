package com.facebook.infer.annotation;

/**
 * Created by hanjiahu on 2017/8/24.
 */
import java.util.List;
import java.util.Map;
import android.support.annotation.Nullable;

public class Assertions {
  public Assertions() {
  }

  public static <T> T assumeNotNull(@Nullable T var0) {
    return var0;
  }

  public static <T> T assumeNotNull(@Nullable T var0, String var1) {
    return var0;
  }

  public static <T> T assertNotNull(@Nullable T var0) {
    if(var0 == null) {
      throw new AssertionError();
    } else {
      return var0;
    }
  }

  public static <T> T assertNotNull(@Nullable T var0, String var1) {
    if(var0 == null) {
      throw new AssertionError(var1);
    } else {
      return var0;
    }
  }

  public static <T> T getAssumingNotNull(List<T> var0, int var1) {
    return var0.get(var1);
  }

  public static <T> T getAssertingNotNull(List<T> var0, int var1) {
    assertCondition(0 <= var1 && var1 < var0.size());
    return assertNotNull(var0.get(var1));
  }

  public static <K, V> V getAssumingNotNull(Map<K, V> var0, K var1) {
    return var0.get(var1);
  }

  public static <K, V> V getAssertingNotNull(Map<K, V> var0, K var1) {
    assertCondition(var0.containsKey(var1));
    return assertNotNull(var0.get(var1));
  }

  public static void assumeCondition(boolean var0) {
  }

  public static void assumeCondition(boolean var0, String var1) {
  }

  public static void assertCondition(boolean var0) {
    if(!var0) {
      throw new AssertionError();
    }
  }

  public static void assertCondition(boolean var0, String var1) {
    if(!var0) {
      throw new AssertionError(var1);
    }
  }

  public static AssertionError assertUnreachable() {
    throw new AssertionError();
  }

  public static AssertionError assertUnreachable(String var0) {
    throw new AssertionError(var0);
  }

  public static AssertionError assertUnreachable(Exception var0) {
    throw new AssertionError(var0);
  }
}
