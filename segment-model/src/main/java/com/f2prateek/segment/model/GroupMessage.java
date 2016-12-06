package com.f2prateek.segment.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static com.f2prateek.segment.model.Utils.assertNotNullOrEmpty;

/**
 * The group API call is how you associate an individual user with a group—be it a company,
 * organization, account, project, team or whatever other crazy name you came up with for the same
 * concept! It also lets you record custom traits about the group, like industry or number of
 * employees. Calling group is a slightly more advanced feature, but it’s helpful if you have
 * accounts with multiple users.
 *
 * @see <a href="https://segment.com/docs/spec/group/">Group</a>
 */
@AutoValue //
public abstract class GroupMessage extends Message {
  public abstract @Nullable String groupId();

  public abstract @Nullable Map<String, Object> traits();

  @NonNull @Override public Builder toBuilder() {
    return new Builder(this);
  }

  public static JsonAdapter<GroupMessage> jsonAdapter(Moshi moshi) {
    return new AutoValue_GroupMessage.MoshiJsonAdapter(moshi);
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
      Utils.assertNotNull(traits, "traits");
      this.traits = Utils.immutableCopyOf(traits);
      return this;
    }

    @Override protected GroupMessage realBuild(Type type, String messageId, Date timestamp,
        Map<String, Object> context, Map<String, Object> integrations, String userId,
        String anonymousId) {
      assertNotNullOrEmpty(groupId, "groupId");

      Map<String, Object> traits = this.traits;
      if (Utils.isNullOrEmpty(traits)) {
        traits = Collections.emptyMap();
      }

      return new AutoValue_GroupMessage(type, messageId, timestamp, context, integrations, userId,
          anonymousId, groupId, traits);
    }

    @Override Builder self() {
      return this;
    }
  }
}