package com.f2prateek.segment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static com.f2prateek.segment.Utils.assertNotNull;
import static com.f2prateek.segment.Utils.assertNotNullOrEmpty;
import static com.f2prateek.segment.Utils.immutableCopyOf;

/**
 * The track API call is how you record any actions your users perform, along with any properties
 * that describe the action.
 *
 * @see <a href="https://segment.com/docs/spec/track/">Track</a>
 */
@AutoValue //
public abstract class TrackMessage extends Message {
  @NonNull public abstract String event();

  @Nullable public abstract Map<String, Object> properties();

  @NonNull @Override public Builder toBuilder() {
    return new Builder(this);
  }

  public static JsonAdapter<TrackMessage> jsonAdapter(Moshi moshi) {
    return new AutoValue_TrackMessage.MoshiJsonAdapter(moshi);
  }

  /** Fluent API for creating {@link TrackMessage} instances. */
  public static class Builder extends Message.Builder<TrackMessage, Builder> {
    private String event;
    private Map<String, Object> properties;

    public Builder(@NonNull Segment segment) {
      super(Type.track, segment);
    }

    @Private Builder(TrackMessage track) {
      super(track);
      event = track.event();
      properties = track.properties();
    }

    @Private Builder() {
      super(Type.track);
    }

    public Builder event(@NonNull String event) {
      this.event = assertNotNullOrEmpty(event, "event");
      postEnqueue();
      return this;
    }

    public Builder properties(@NonNull Map<String, Object> properties) {
      assertNotNull(properties, "properties");
      this.properties = immutableCopyOf(properties);
      postEnqueue();
      return this;
    }

    @Override protected TrackMessage realBuild(Type type, String messageId, Date timestamp,
        Map<String, Object> context, Map<String, Object> integrations, String userId,
        String anonymousId, Segment segment) {
      assertNotNullOrEmpty(event, "event");

      Map<String, Object> properties = this.properties;
      if (Utils.isNullOrEmpty(properties)) {
        properties = Collections.emptyMap();
      }

      return new AutoValue_TrackMessage(type, messageId, timestamp, context, integrations, userId,
          anonymousId, event, properties);
    }

    @Override Builder self() {
      return this;
    }
  }
}
