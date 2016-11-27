package com.f2prateek.segment;

import android.Manifest;
import android.app.Application;
import android.support.annotation.Nullable;
import com.squareup.moshi.Moshi;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class) //
@Config(constants = BuildConfig.class, sdk = 23) //
public class SegmentTest {
  @Rule public final MockWebServer server = new MockWebServer();

  private static void grantPermission(final Application app, final String permission) {
    ShadowApplication shadowApp = Shadows.shadowOf(app);
    shadowApp.grantPermissions(permission);
  }

  @Before public void setUp() {
    grantPermission(RuntimeEnvironment.application, Manifest.permission.INTERNET);
  }

  @Test public void interceptors() throws Exception {
    Interceptor interceptor = Mockito.spy(new Interceptor() {
      @Nullable @Override public Future<Message> intercept(Chain chain) {
        return chain.proceed(chain.message());
      }
    });

    Segment segment = new Segment.Builder() //
        .writeKey("writeKey") //
        .context(RuntimeEnvironment.application) //
        .baseUrl(server.url("/")) //
        .interceptor(interceptor) //
        .build();

    List<Message> messages =
        Arrays.asList(segment.newAlias("newId").build(), segment.newGroup("groupId").build(),
            segment.newIdentify("userId").build(), segment.newScreen("name").build(),
            segment.newTrack("event").build());

    for (Message m : messages) {
      Mockito.reset(interceptor);

      segment.enqueue(m).get();

      ArgumentCaptor<Interceptor.Chain> chainArgumentCaptor =
          ArgumentCaptor.forClass(Interceptor.Chain.class);
      verify(interceptor).intercept(chainArgumentCaptor.capture());
      assertThat(chainArgumentCaptor.getValue().message()).isEqualTo(m);
    }
  }

  @Test public void e2e() throws Exception {
    Segment segment = new Segment.Builder() //
        .writeKey("writeKey") //
        .context(RuntimeEnvironment.application) //
        .baseUrl(server.url("/")) //
        .build();

    List<Message> messages =
        Arrays.asList(segment.newAlias("newId").build(), segment.newGroup("groupId").build(),
            segment.newIdentify("userId").build(), segment.newScreen("name").build(),
            segment.newTrack("event").build());

    for (Message m : messages) {
      server.enqueue(new MockResponse());

      segment.enqueue(m).get();
      segment.flush().get();

      RecordedRequest request = server.takeRequest();
      assertThat(request.getRequestLine()).isEqualTo("POST /v1/batch HTTP/1.1");
      //noinspection SpellCheckingInspection
      assertThat(request.getHeader("Authorization")).isEqualTo("Basic d3JpdGVLZXk6");

      // TODO: Use proper fixtures. Currently IDs and timestamps are not consistent between runs.
      final Moshi moshi = SegmentMoshiAdapterFactory.MOSHI;
      Batch batch = moshi.adapter(Batch.class).fromJson(request.getBody());
      assertThat(batch.batch()).hasSize(1);
      assertThat(batch.batch().get(0)).isEqualTo(m);
    }
  }

  @Test public void disallowsLargeMessages() throws InterruptedException {
    Segment segment = new Segment.Builder() //
        .writeKey("writeKey") //
        .context(RuntimeEnvironment.application) //
        .baseUrl(server.url("/")) //
        .build();

    Map<String, Object> properties = new LinkedHashMap<>();
    for (int i = 0; i < 375; i++) {
      properties.put("prop_" + i, "abcdefghijklmnopqrstuvwxyz");
    }

    try {
      segment.enqueue(segment.newTrack("event").properties(properties).build()).get();
      fail();
    } catch (ExecutionException e) {
      assertThat(e.getCause()).isInstanceOf(MoshiMessageConverter.MessageTooLargeException.class);
    }
  }
}
