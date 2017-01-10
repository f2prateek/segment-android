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
 * The screen call lets you record whenever a user sees a screen, along with any properties about
 * the screen.
 *
 * @see <a href="https://segment.com/docs/spec/screen/">Screen</a>
 */
public final class ScreenMessage extends Message {
  private @NonNull final String name;
  private @Nullable final Map<String, Object> properties;

  @Private ScreenMessage(Type type, String messageId, Date timestamp, Map<String, Object> context,
      Map<String, Object> integrations, String userId, String anonymousId, @NonNull String name,
      @Nullable Map<String, Object> properties) {
    super(type, messageId, timestamp, context, integrations, userId, anonymousId);
    this.name = name;
    this.properties = properties;
  }

  public @NonNull String name() {
    return name;
  }

  public @Nullable Map<String, Object> properties() {
    return properties;
  }

  @Override public @NonNull Builder toBuilder() {
    return new Builder(this);
  }

  @Override public String toString() {
    return "ScreenMessage{"
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
        + "name="
        + name
        + ", "
        + "properties="
        + properties
        + "}";
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ScreenMessage) {
      ScreenMessage that = (ScreenMessage) o;
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
          && (this.name.equals(that.name()))
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
    h ^= this.name.hashCode();
    h *= 1000003;
    h ^= (properties == null) ? 0 : this.properties.hashCode();
    return h;
  }

  /** Fluent API for creating {@link ScreenMessage} instances. */
  public static class Builder extends Message.Builder<ScreenMessage, Builder> {
    private String name;
    private Map<String, Object> properties;

    public Builder() {
      super(Type.screen);
    }

    @Private Builder(ScreenMessage screen) {
      super(screen);
      name = screen.name();
      properties = screen.properties();
    }

    public @NonNull Builder name(@NonNull String name) {
      this.name = assertNotNullOrEmpty(name, "name");
      return this;
    }

    public @NonNull Builder properties(@NonNull Map<String, Object> properties) {
      assertNotNull(properties, "properties");
      this.properties = immutableCopyOf(properties);
      return this;
    }

    @Override protected ScreenMessage realBuild(Type type, String messageId, Date timestamp,
        Map<String, Object> context, Map<String, Object> integrations, String userId,
        String anonymousId) {
      assertNotNullOrEmpty(name, "name");

      Map<String, Object> properties = this.properties;
      if (isNullOrEmpty(properties)) {
        properties = Collections.emptyMap();
      }

      return new ScreenMessage(type, messageId, timestamp, context, integrations, userId,
          anonymousId, name, properties);
    }

    @Override Builder self() {
      return this;
    }
  }
}
