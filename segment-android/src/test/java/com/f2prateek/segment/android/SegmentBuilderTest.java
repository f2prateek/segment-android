package com.f2prateek.segment.android;

import android.Manifest;
import android.app.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

@SuppressWarnings("CheckResult") //
@RunWith(RobolectricTestRunner.class) //
@Config(constants = BuildConfig.class, sdk = 23) //
public class SegmentBuilderTest {

  @Test(expected = IllegalStateException.class) //
  public void duplicateInterceptor() throws Exception {
    Interceptor interceptor = Mockito.mock(Interceptor.class);

    new Segment.Builder().interceptor(interceptor).interceptor(interceptor);
  }

  @Test(expected = NullPointerException.class) public void nullInterceptor() throws Exception {
    new Segment.Builder().interceptor(null);
  }

  @Test(expected = IllegalArgumentException.class) public void invalidContext() throws Exception {
    new Segment.Builder().context(RuntimeEnvironment.application);
  }

  @Test(expected = NullPointerException.class) public void nullClient() throws Exception {
    new Segment.Builder().client(null);
  }

  @Test(expected = NullPointerException.class) public void nullQueue() throws Exception {
    new Segment.Builder().queue(null);
  }

  @Test(expected = IllegalArgumentException.class) public void invalidBaseUrl() throws Exception {
    new Segment.Builder().baseUrl("foo");
  }

  @Test public void validBaseUrl() throws Exception {
    new Segment.Builder().baseUrl("https://google.com");
  }

  @Test(expected = NullPointerException.class) public void nullBaseUrl() throws Exception {
    new Segment.Builder().baseUrl((String) null);
  }

  @Test(expected = NullPointerException.class) public void nullCallback() throws Exception {
    new Segment.Builder().callback(null);
  }

  @Test public void builderWithDefaults() {
    grantPermission(RuntimeEnvironment.application, Manifest.permission.INTERNET);
    new Segment.Builder().context(RuntimeEnvironment.application).writeKey("foo").build();
  }

  private static void grantPermission(final Application app, final String permission) {
    ShadowApplication shadowApp = Shadows.shadowOf(app);
    shadowApp.grantPermissions(permission);
  }
}
