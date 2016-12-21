package com.f2prateek.segment.model;

import android.support.annotation.NonNull;
import java.util.Date;
import java.util.Map;

import static com.f2prateek.segment.model.Utils.assertNotNullOrEmpty;

/**
 * The alias message is used to merge two user identities, effectively connecting two sets of user
 * data as one. This is an advanced method, but it is required to manage user identities
 * successfully in some of our integrations.
 *
 * @see <a href="https://segment.com/docs/spec/alias/">Alias</a>
 */
public final class AliasMessage extends Message {
  private final @NonNull String previousId;

  @Private AliasMessage(Message.Type type, String messageId, Date timestamp,
      Map<String, Object> context, Map<String, Object> integrations, String userId,
      String anonymousId, @NonNull String previousId) {
    super(type, messageId, timestamp, context, integrations, userId, anonymousId);
    this.previousId = previousId;
  }

  public @NonNull String previousId() {
    return previousId;
  }

  @Override public @NonNull Builder toBuilder() {
    return new Builder(this);
  }

  @Override public String toString() {
    return "AliasMessage{"
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
        + "previousId="
        + previousId
        + "}";
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof AliasMessage) {
      AliasMessage that = (AliasMessage) o;
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
          && (this.anonymousId.equals(that.anonymousId()))
          && (this.previousId.equals(that.previousId()));
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
    h ^= this.previousId.hashCode();
    return h;
  }

  /** Fluent API for creating {@link AliasMessage} instances. */
  public final static class Builder extends Message.Builder<AliasMessage, Builder> {
    private String previousId;

    public Builder() {
      super(Type.alias);
    }

    @Private Builder(AliasMessage alias) {
      super(alias);
      this.previousId = alias.previousId();
    }

    public @NonNull Builder previousId(@NonNull String previousId) {
      this.previousId = assertNotNullOrEmpty(previousId, "previousId");
      return this;
    }

    @Override protected AliasMessage realBuild(Type type, String messageId, Date timestamp,
        Map<String, Object> context, Map<String, Object> integrations, String userId,
        String anonymousId) {
      assertNotNullOrEmpty(userId, "userId");
      assertNotNullOrEmpty(previousId, "previousId");

      return new AliasMessage(type, messageId, timestamp, context, integrations, userId,
          anonymousId, previousId);
    }

    @Override Builder self() {
      return this;
    }
  }
}