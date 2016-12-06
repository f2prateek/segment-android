package com.f2prateek.segment.analytics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Map;

final class Utils {
  private Utils() {
    throw new AssertionError("No instances.");
  }

  /** Throws a {@link NullPointerException} if the given object is null. */
  static @NonNull <T> T assertNotNull(T object, String item) {
    if (object == null) {
      throw new NullPointerException(item + " == null");
    }
    return object;
  }

  /** Throws a {@link NullPointerException} if the given text is null or empty. */
  static @NonNull String assertNotNullOrEmpty(String text, @Nullable String name) {
    if (isNullOrEmpty(text)) {
      throw new NullPointerException(name + " cannot be null or empty");
    }
    return text;
  }

  /** Returns {@code true} if the string is null, or empty (once trimmed). */
  static boolean isNullOrEmpty(String text) {
    return text == null || text.length() == 0 || text.trim().length() == 0;
  }

  /** Returns true if the map is null or empty. */
  static boolean isNullOrEmpty(Map<?, ?> map) {
    return map == null || map.size() == 0;
  }
}