package com.f2prateek.segment.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Date;
import java.util.Map;

import static com.f2prateek.segment.model.Utils.assertNotNull;
import static com.f2prateek.segment.model.Utils.immutableCopyOf;
import static com.f2prateek.segment.model.Utils.isNullOrEmpty;

/**
 * The identify call ties a customer and their actions to a recognizable ID and traits like their
 * email, name, etc.
 *
 * @see <a href="https://segment.com/docs/spec/identify/">Identify</a>
 */
public final class IdentifyMessage extends Message {
  private @Nullable final Map<String, Object> traits;

  @Private IdentifyMessage(Type type, String messageId, Date timestamp, Map<String, Object> context,
      Map<String, Object> integrations, String userId, String anonymousId,
      @Nullable Map<String, Object> traits) {
    super(type, messageId, timestamp, context, integrations, userId, anonymousId);
    this.traits = traits;
  }

  public @Nullable Map<String, Object> traits() {
    return traits;
  }

  @Override public @NonNull Builder toBuilder() {
    return new Builder(this);
  }

  @Override public String toString() {
    return "IdentifyMessage{"
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
        + "traits="
        + traits
        + "}";
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof IdentifyMessage) {
      IdentifyMessage that = (IdentifyMessage) o;
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
          && ((this.traits == null) ? (that.traits() == null) : this.traits.equals(that.traits()));
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
    h ^= (traits == null) ? 0 : this.traits.hashCode();
    return h;
  }

  /** Fluent API for creating {@link IdentifyMessage} instances. */
  public static class Builder extends Message.Builder<IdentifyMessage, Builder> {
    private Map<String, Object> traits;

    public Builder() {
      super(Type.identify);
    }

    @Private Builder(IdentifyMessage identify) {
      super(identify);
      traits = identify.traits();
    }

    public @NonNull Builder traits(@NonNull Map<String, Object> traits) {
      assertNotNull(traits, "traits");
      this.traits = immutableCopyOf(traits);
      return this;
    }

    @Override protected IdentifyMessage realBuild(Type type, String messageId, Date timestamp,
        Map<String, Object> context, Map<String, Object> integrations, String userId,
        String anonymousId) {
      if (isNullOrEmpty(userId) && isNullOrEmpty(anonymousId) && isNullOrEmpty(anonymousId)) {
        throw new NullPointerException("either userId or anonymousId or traits are required");
      }

      return new IdentifyMessage(type, messageId, timestamp, context, integrations, userId,
          anonymousId, traits);
    }

    @Override Builder self() {
      return this;
    }
  }
}