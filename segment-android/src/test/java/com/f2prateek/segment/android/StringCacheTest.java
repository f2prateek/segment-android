package com.f2prateek.segment.android;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class) //
@Config(constants = BuildConfig.class, sdk = 23) //
public class StringCacheTest {
  SharedPreferences sharedPreferences;
  StringCache stringCache;

  @Before public void setUp() {
    sharedPreferences =
        RuntimeEnvironment.application.getSharedPreferences("cache_test", Context.MODE_PRIVATE);
    sharedPreferences.edit().clear().apply();
    stringCache = new StringCache(sharedPreferences, "foo");
  }

  @Test public void cacheTest() {
    assertThat(stringCache.get()).isEqualTo(null);

    stringCache.set("bar");
    assertThat(sharedPreferences).contains("foo", "bar");
    assertThat(stringCache.get()).isEqualTo("bar");

    stringCache.clear();
    assertThat(sharedPreferences).doesNotHaveKey("foo");
    assertThat(stringCache.get()).isEqualTo(null);
  }
}
