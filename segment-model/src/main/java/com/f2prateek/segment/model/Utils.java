package com.f2prateek.segment.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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

  /** Throws a {@link NullPointerException} if the given list is null or empty. */
  static @NonNull <T> List<T> assertNotNullOrEmpty(List<T> data, @Nullable String name) {
    if (isNullOrEmpty(data)) {
      throw new NullPointerException(name + " cannot be null or empty");
    }
    return data;
  }

  /** Returns {@code true} if the string is null, or empty (once trimmed). */
  static boolean isNullOrEmpty(String text) {
    return text == null || text.length() == 0 || text.trim().length() == 0;
  }

  /** Returns true if the map is null or empty. */
  static boolean isNullOrEmpty(Map<?, ?> map) {
    return map == null || map.size() == 0;
  }

  /** Returns true if the list is null or empty. */
  static boolean isNullOrEmpty(List<?> list) {
    return list == null || list.size() == 0;
  }

  /** Returns an immutable copy of the provided map. */
  static @NonNull <K, V> Map<K, V> immutableCopyOf(@NonNull Map<K, V> map) {
    return Collections.unmodifiableMap(new LinkedHashMap<>(map));
  }
}