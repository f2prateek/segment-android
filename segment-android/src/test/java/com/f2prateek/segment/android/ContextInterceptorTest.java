package com.f2prateek.segment.android;

import com.f2prateek.segment.model.Message;
import com.f2prateek.segment.model.TrackMessage;
import java.util.Map;
import java.util.concurrent.Future;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("CheckResult") //
@RunWith(RobolectricTestRunner.class) //
@Config(constants = BuildConfig.class, sdk = 23) //
public class ContextInterceptorTest {
  @Test public void contextInterceptor() {
    Interceptor.Chain chain = mock(Interceptor.Chain.class);
    ArgumentCaptor<Message> argumentCaptor = ArgumentCaptor.forClass(Message.class);
    when(chain.message()).thenReturn(new TrackMessage.Builder() //
        .userId("foo").event("event").build());
    when(chain.proceed(argumentCaptor.capture())).thenReturn(null);

    Interceptor interceptor = ContextInterceptor.with(RuntimeEnvironment.application);
    interceptor.intercept(chain);

    Map<String, Object> context = argumentCaptor.getValue().context();
    assertThat(context).hasSize(9);
    assertThat((Map<String, Object>) context.get("app")) //
        .containsEntry("name", "com.f2prateek.segment.android")
        .containsEntry("version", null)
        .containsEntry("build", 0);
    assertThat((Map<String, Object>) context.get("device")) //
        .containsEntry("id", "unknown")
        .containsEntry("manufacturer", "unknown")
        .containsEntry("model", "unknown")
        .containsEntry("name", "unknown");
    assertThat((Map<String, Object>) context.get("network")) //
        .containsEntry("carrier", null);
    assertThat((Map<String, Object>) context.get("screen")) //
        .containsEntry("density", 1.5f)
        .containsEntry("height", 800)
        .containsEntry("width", 480);
    assertThat((Map<String, Object>) context.get("library")) //
        .containsEntry("name", "segment-android")
        .containsEntry("version", BuildConfig.VERSION_NAME);
    assertThat((Map<String, Object>) context.get("os")) //
        .containsEntry("name", "Android")
        .containsEntry("version", "6.0.0_r1");
    assertThat((String) context.get("userAgent")).isNull();
    // Timezone depends on the computer running so ignore.
    // assertThat((String) context.get("timezone")).isEqualTo("America/Vancouver);
    assertThat((String) context.get("locale")).isEqualTo("en-US");
  }

  static class MockChain implements Interceptor.Chain {
    @Override public Message message() {
      return null;
    }

    @Override public Future<Message> proceed(Message message) {
      return null;
    }
  }
}
