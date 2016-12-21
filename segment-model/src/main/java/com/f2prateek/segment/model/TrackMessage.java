package com.f2prateek.segment.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
public final class TrackMessage extends Message {
  private @NonNull final String event;
  private @Nullable final Map<String, Object> properties;

  @Private TrackMessage(Type type, String messageId, Date timestamp, Map<String, Object> context,
      Map<String, Object> integrations, String userId, String anonymousId, @NonNull String event,
      @Nullable Map<String, Object> properties) {
    super(type, messageId, timestamp, context, integrations, userId, anonymousId);
    this.event = event;
    this.properties = properties;
  }

  public @NonNull String event() {
    return event;
  }

  public @Nullable Map<String, Object> properties() {
    return properties;
  }

  @Override public @NonNull Builder toBuilder() {
    return new Builder(this);
  }

  @Override public String toString() {
    return "TrackMessage{"
        + "type="
        + type
        + ", "
        + "messageId="
        + messageId
        + ", "
        + "timestamp="
        + timestamp
        + ", "
        + "context="
        + context
        + ", "
        + "integrations="
        + integrations
        + ", "
        + "userId="
        + userId
        + ", "
        + "anonymousId="
        + anonymousId
        + ", "
        + "event="
        + event
        + ", "
        + "properties="
        + properties
        + "}";
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TrackMessage) {
      TrackMessage that = (TrackMessage) o;
      return (this.type.equals(that.type()))
          && ((this.messageId == null) ? (that.messageId()
          == null) : this.messageId.equals(that.messageId()))
          && ((this.timestamp == null) ? (that.timestamp() == null)
          : this.timestamp.equals(that.timestamp()))
          && ((this.context == null) ? (that.context() == null)
          : this.context.equals(that.context()))
          && ((this.integrations == null) ? (that.integrations() == null)
          : this.integrations.equals(that.integrations()))
          && ((this.userId == null) ? (that.userId() == null) : this.userId.equals(that.userId()))
          && ((this.anonymousId == null) ? (that.anonymousId() == null)
          : this.anonymousId.equals(that.anonymousId()))
          && (this.event.equals(that.event()))
          && ((this.properties == null) ? (that.properties() == null)
          : this.properties.equals(that.properties()));
    }
    return false;
  }

  @Override public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= this.type.hashCode();
    h *= 1000003;
    h ^= (messageId == null) ? 0 : this.messageId.hashCode();
    h *= 1000003;
    h ^= (timestamp == null) ? 0 : this.timestamp.hashCode();
    h *= 1000003;
    h ^= (context == null) ? 0 : this.context.hashCode();
    h *= 1000003;
    h ^= (integrations == null) ? 0 : this.integrations.hashCode();
    h *= 1000003;
    h ^= (userId == null) ? 0 : this.userId.hashCode();
    h *= 1000003;
    h ^= (anonymousId == null) ? 0 : this.anonymousId.hashCode();
    h *= 1000003;
    h ^= this.event.hashCode();
    h *= 1000003;
    h ^= (properties == null) ? 0 : this.properties.hashCode();
    return h;
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

      return new TrackMessage(type, messageId, timestamp, context, integrations, userId,
          anonymousId, event, properties);
    }

    @Override Builder self() {
      return this;
    }
  }
}
