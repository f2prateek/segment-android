package com.f2prateek.segment.analytics;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class StringCache {
  private final SharedPreferences sharedPreferences;
  private final String key;

  StringCache(SharedPreferences sharedPreferences, String key) {
    this.sharedPreferences = sharedPreferences;
    this.key = key;
  }

  void set(@NonNull String s) {
    sharedPreferences.edit().putString(key, s).apply();
  }

  @Nullable String get() {
    return sharedPreferences.getString(key, null);
  }

  void clear() {
    sharedPreferences.edit().remove(key).apply();
  }
}
