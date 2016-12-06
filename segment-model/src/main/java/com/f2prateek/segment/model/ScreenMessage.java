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
 * The screen call lets you record whenever a user sees a screen, along with any properties about
 * the screen.
 *
 * @see <a href="https://segment.com/docs/spec/screen/">Screen</a>
 */
@AutoValue //
public abstract class ScreenMessage extends Message {
  public abstract @NonNull String name();

  public abstract @Nullable Map<String, Object> properties();

  @Override public @NonNull Builder toBuilder() {
    return new Builder(this);
  }

  public static JsonAdapter<ScreenMessage> jsonAdapter(Moshi moshi) {
    return new AutoValue_ScreenMessage.MoshiJsonAdapter(moshi);
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

      return new AutoValue_ScreenMessage(type, messageId, timestamp, context, integrations, userId,
          anonymousId, name, properties);
    }

    @Override Builder self() {
      return this;
    }
  }
}