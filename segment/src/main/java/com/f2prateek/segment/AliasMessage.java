package com.f2prateek.segment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Date;
import java.util.Map;

import static com.f2prateek.segment.Utils.assertNotNullOrEmpty;

/**
 * The alias message is used to merge two user identities, effectively connecting two sets of user
 * data as one. This is an advanced method, but it is required to manage user identities
 * successfully in some of our integrations.
 *
 * @see <a href="https://segment.com/docs/spec/alias/">Alias</a>
 */
@AutoValue //
public abstract class AliasMessage extends Message {
  @Nullable abstract String previousId();

  @NonNull @Override public Builder toBuilder() {
    return new Builder(this);
  }

  public static JsonAdapter<AliasMessage> jsonAdapter(Moshi moshi) {
    return new AutoValue_AliasMessage.MoshiJsonAdapter(moshi);
  }

  /** Fluent API for creating {@link AliasMessage} instances. */
  public static class Builder extends Message.Builder<AliasMessage, Builder> {
    private String previousId;

    Builder() {
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

      return new AutoValue_AliasMessage(type, messageId, timestamp, context, integrations, userId,
          anonymousId, previousId);
    }

    @Override Builder self() {
      return this;
    }
  }
}