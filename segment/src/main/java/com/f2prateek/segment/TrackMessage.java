package com.f2prateek.segment;

import android.support.annotation.CheckResult;
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
import static com.f2prateek.segment.Utils.isNullOrEmpty;

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

    Builder() {
      super(Type.track);
    }

    @Private Builder(TrackMessage track) {
      super(track);
      event = track.event();
      properties = track.properties();
    }

    @CheckResult public @NonNull Builder event(@NonNull String event) {
      this.event = assertNotNullOrEmpty(event, "event");
      return this;
    }

    @CheckResult public Builder properties(@NonNull Map<String, Object> properties) {
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
