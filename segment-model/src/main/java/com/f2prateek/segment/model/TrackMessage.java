package com.f2prateek.segment.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static com.f2prateek.segment.model.Utils.assertNotNull;
import static com.f2prateek.segment.model.Utils.assertNotNullOrEmpty;
import static com.f2prateek.segment.model.Utils.immutableCopyOf;
import static com.f2prateek.segment.model.Utils.isNullOrEmpty;

/**
 * The track API call is how you record any actions your users perform, along with any properties
 * that describe the action.
 *
 * @see <a href="https://segment.com/docs/spec/track/">Track</a>
 */
@AutoValue //
public abstract class TrackMessage extends Message {
  public abstract @NonNull String event();

  public abstract @Nullable Map<String, Object> properties();

  @Override public @NonNull Builder toBuilder() {
    return new Builder(this);
  }

  public static JsonAdapter<TrackMessage> jsonAdapter(Moshi moshi) {
    return new AutoValue_TrackMessage.MoshiJsonAdapter(moshi);
  }

  /** Fluent API for creating {@link TrackMessage} instances. */
  public static class Builder extends Message.Builder<TrackMessage, Builder> {
    private String event;
    private Map<String, Object> properties;

    public Builder() {
      super(Type.track);
    }

    @Private Builder(TrackMessage track) {
      super(track);
      event = track.event();
      properties = track.properties();
    }

    public @NonNull Builder event(@NonNull String event) {
      this.event = assertNotNullOrEmpty(event, "event");
      return this;
    }

    public Builder properties(@NonNull Map<String, Object> properties) {
      assertNotNull(properties, "properties");
      this.properties = immutableCopyOf(properties);
      return this;
    }

    @Override protected TrackMessage realBuild(Type type, String messageId, Date timestamp,
        Map<String, Object> context, Map<String, Object> integrations, String userId,
        String anonymousId) {
      assertNotNullOrEmpty(event, "event");

      Map<String, Object> properties = this.properties;
      if (isNullOrEmpty(properties)) {
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
