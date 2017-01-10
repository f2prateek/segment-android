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
 * The group API call is how you associate an individual user with a group—be it a company,
 * organization, account, project, team or whatever other crazy name you came up with for the same
 * concept! It also lets you record custom traits about the group, like industry or number of
 * employees. Calling group is a slightly more advanced feature, but it’s helpful if you have
 * accounts with multiple users.
 *
 * @see <a href="https://segment.com/docs/spec/group/">Group</a>
 */
public final class GroupMessage extends Message {
  private @NonNull final String groupId;
  private @Nullable final Map<String, Object> traits;

  @Private GroupMessage(Type type, String messageId, Date timestamp, Map<String, Object> context,
      Map<String, Object> integrations, String userId, String anonymousId, @NonNull String groupId,
      @Nullable Map<String, Object> traits) {
    super(type, messageId, timestamp, context, integrations, userId, anonymousId);
    this.groupId = groupId;
    this.traits = traits;
  }

  public @NonNull String groupId() {
    return groupId;
  }

  public @Nullable Map<String, Object> traits() {
    return traits;
  }

  @NonNull @Override public Builder toBuilder() {
    return new Builder(this);
  }

  @Override public String toString() {
    return "GroupMessage{"
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
        + "groupId="
        + groupId
        + ", "
        + "traits="
        + traits
        + "}";
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof GroupMessage) {
      GroupMessage that = (GroupMessage) o;
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
          && (this.groupId.equals(that.groupId()))
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
    h ^= this.groupId.hashCode();
    h *= 1000003;
    h ^= (traits == null) ? 0 : this.traits.hashCode();
    return h;
  }

  /** Fluent API for creating {@link GroupMessage} instances. */
  public static class Builder extends Message.Builder<GroupMessage, Builder> {
    private String groupId;
    private Map<String, Object> traits;

    public Builder() {
      super(Type.group);
    }

    @Private Builder(GroupMessage group) {
      super(group);
      groupId = group.groupId();
      traits = group.traits();
    }

    public @NonNull Builder groupId(@NonNull String groupId) {
      this.groupId = assertNotNullOrEmpty(groupId, "groupId");
      return this;
    }

    public @NonNull Builder traits(@NonNull Map<String, Object> traits) {
      assertNotNull(traits, "traits");
      this.traits = immutableCopyOf(traits);
      return this;
    }

    @Override protected GroupMessage realBuild(Type type, String messageId, Date timestamp,
        Map<String, Object> context, Map<String, Object> integrations, String userId,
        String anonymousId) {
      assertNotNullOrEmpty(groupId, "groupId");

      Map<String, Object> traits = this.traits;
      if (isNullOrEmpty(traits)) {
        traits = Collections.emptyMap();
      }

      return new GroupMessage(type, messageId, timestamp, context, integrations, userId,
          anonymousId, groupId, traits);
    }

    @Override Builder self() {
      return this;
    }
  }
}