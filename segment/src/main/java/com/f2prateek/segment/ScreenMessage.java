package com.f2prateek.segment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static com.f2prateek.segment.Utils.assertNotNullOrEmpty;
import static com.f2prateek.segment.Utils.immutableCopyOf;

/**
 * The screen call lets you record whenever a user sees a screen, along with any properties about
 * the screen.
 *
 * @see <a href="https://segment.com/docs/spec/screen/">Screen</a>
 */
@AutoValue //
public abstract class ScreenMessage extends Message {
  @NonNull public abstract String name();

  @Nullable public abstract Map<String, Object> properties();

  @NonNull @Override public Builder toBuilder() {
    return new Builder(this);
  }

  public static JsonAdapter<ScreenMessage> jsonAdapter(Moshi moshi) {
    return new AutoValue_ScreenMessage.MoshiJsonAdapter(moshi);
  }

  /** Fluent API for creating {@link ScreenMessage} instances. */
  public static class Builder extends Message.Builder<ScreenMessage, Builder> {
    private String name;
    private Map<String, Object> properties;

    public Builder(@NonNull Segment segment) {
      super(Type.screen, segment);
    }

    @Private Builder(ScreenMessage screen) {
      super(screen);
      name = screen.name();
      properties = screen.properties();
    }

    @Private Builder() {
      super(Type.screen);
    }

    public Builder name(@NonNull String name) {
      this.name = assertNotNullOrEmpty(name, "name");
      postEnqueue();
      return this;
    }

    public Builder properties(@NonNull Map<String, Object> properties) {
      Utils.assertNotNull(properties, "properties");
      this.properties = immutableCopyOf(properties);
      postEnqueue();
      return this;
    }

    @Override protected ScreenMessage realBuild(Type type, String messageId, Date timestamp,
        Map<String, Object> context, Map<String, Object> integrations, String userId,
        String anonymousId, Segment segment) {
      assertNotNullOrEmpty(name, "name");

      Map<String, Object> properties = this.properties;
      if (Utils.isNullOrEmpty(properties)) {
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
