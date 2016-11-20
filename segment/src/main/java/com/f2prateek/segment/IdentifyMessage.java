package com.f2prateek.segment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Date;
import java.util.Map;

import static com.f2prateek.segment.Utils.assertNotNull;
import static com.f2prateek.segment.Utils.immutableCopyOf;
import static com.f2prateek.segment.Utils.isNullOrEmpty;

/**
 * The identify call ties a customer and their actions to a recognizable ID and traits like their
 * email, name, etc.
 *
 * @see <a href="https://segment.com/docs/spec/identify/">Identify</a>
 */
@AutoValue //
public abstract class IdentifyMessage extends Message {
  @Nullable public abstract Map<String, Object> traits();

  @NonNull @Override public Builder toBuilder() {
    return new Builder(this);
  }

  public static JsonAdapter<IdentifyMessage> jsonAdapter(Moshi moshi) {
    return new AutoValue_IdentifyMessage.MoshiJsonAdapter(moshi);
  }

  /** Fluent API for creating {@link IdentifyMessage} instances. */
  public static class Builder extends Message.Builder<IdentifyMessage, Builder> {
    private Map<String, Object> traits;

    Builder() {
      super(Type.identify);
    }

    @Private Builder(IdentifyMessage identify) {
      super(identify);
      traits = identify.traits();
    }

    public Builder traits(@NonNull Map<String, Object> traits) {
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

      return new AutoValue_IdentifyMessage(type, messageId, timestamp, context, integrations,
          userId, anonymousId, traits);
    }

    @Override Builder self() {
      return this;
    }
  }
}