package com.f2prateek.segment.android;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.support.annotation.Nullable;
import com.f2prateek.segment.model.Message;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
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

import static org.assertj.core.api.Assertions.assertThat;
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
            segment.newIdentify("userId").build(),
            segment.newIdentify(new LinkedHashMap<String, Object>()).build(),
            segment.newScreen("name").build(), segment.newTrack("event").build());

    for (Message m : messages) {
      Mockito.reset(interceptor);

      //noinspection ConstantConditions
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

      //noinspection ConstantConditions
      segment.enqueue(m).get();
      segment.flush().get();

      RecordedRequest request = server.takeRequest();
      assertThat(request.getRequestLine()).isEqualTo("POST /v1/batch HTTP/1.1");
      //noinspection SpellCheckingInspection
      assertThat(request.getHeader("Authorization")).isEqualTo("Basic d3JpdGVLZXk6");

      // TODO: Use proper fixtures.
      assertThat(request.getBody().readUtf8()).contains(m.messageId());
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP) @Test public void logout() throws Exception {
    final Queue<Message> messageQueue = new ConcurrentLinkedDeque<>();
    Segment segment = new Segment.Builder() //
        .writeKey("writeKey") //
        .context(RuntimeEnvironment.application) //
        .interceptor(new Interceptor() {
          @Nullable @Override public Future<Message> intercept(Chain chain) {
            Message message = chain.message();
            messageQueue.add(message);
            return chain.proceed(message);
          }
        }).baseUrl(server.url("/")) //
        .build();

    server.enqueue(new MockResponse());
    segment.enqueue(segment.newIdentify("prateek").build()).get();
    Message first = messageQueue.remove();
    assertThat(first.userId()).isEqualTo("prateek");
    String firstAnonymousId = first.anonymousId();

    segment.logout();

    server.enqueue(new MockResponse());
    segment.enqueue(segment.newIdentify("not prateek").build()).get();
    Message second = messageQueue.remove();
    assertThat(second.userId()).isEqualTo("not prateek");
    assertThat(second.anonymousId()).isNotEqualTo(firstAnonymousId);
  }
}
