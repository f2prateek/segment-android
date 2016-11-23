package com.f2prateek.segment;

import android.Manifest;
import android.app.Application;
import com.squareup.moshi.Moshi;
import java.util.Arrays;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class) //
@Config(constants = BuildConfig.class, sdk = 23) //
public class SegmentTest {
  @Rule public final MockWebServer server = new MockWebServer();
  private Segment segment;

  private static void grantPermission(final Application app, final String permission) {
    ShadowApplication shadowApp = Shadows.shadowOf(app);
    shadowApp.grantPermissions(permission);
  }

  @Before public void setUp() {
    grantPermission(RuntimeEnvironment.application, Manifest.permission.INTERNET);

    segment = new Segment.Builder() //
        .writeKey("writeKey") //
        .context(RuntimeEnvironment.application) //
        .baseUrl(server.url("/")) //
        .build();
  }

  @Test public void e2e() throws Exception {

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
}
