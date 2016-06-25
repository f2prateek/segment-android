package com.f2prateek.segment;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import static com.f2prateek.segment.Utils.assertNotNull;
import static com.f2prateek.segment.Utils.assertNotNullOrEmpty;
import static com.f2prateek.segment.Utils.hasPermission;
import static com.f2prateek.segment.Utils.immutableCopyOf;
import static com.f2prateek.segment.Utils.isNullOrEmpty;

/** Encapsulates properties common to all messages. */
public abstract class Message {
  @Private static final Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());

  public enum Type {
    identify, group, track, screen, alias
  }

  @NonNull abstract Type type();

  @Nullable abstract String messageId();

  @Nullable abstract Date timestamp();

  @Nullable abstract Map<String, Object> context();

  @Nullable abstract Map<String, Object> integrations();

  @Nullable abstract String userId();

  @Nullable abstract String anonymousId();

  @NonNull public abstract Builder toBuilder();

  /** Fluent API to construct instances of a {@link Message}. */
  static abstract class Builder<T extends Message, V extends Builder> {
    @NonNull final Message.Type type;
    String messageId;
    Date timestamp;
    Map<String, Object> context;
    Map<String, Object> integrationsBuilder;
    String userId;
    String anonymousId;

    @Nullable final Segment segment;
    @Nullable final Handler handler;
    final Runnable enqueueRunnable = new Runnable() {
      @Override public void run() {
        enqueue();
      }
    };

    Builder(@NonNull Type type) {
      this.type = type;
      segment = null;
      handler = null;
    }

    Builder(@NonNull Type type, @NonNull Segment segment) {
      this.type = type;
      this.segment = segment;
      if (Looper.myLooper() == null || Looper.myLooper() == Looper.getMainLooper()) {
        handler = MAIN_THREAD_HANDLER;
      } else {
        handler = new Handler();
      }

      String anonymousId = assertNotNullOrEmpty(segment.anonymousIdCache.get(), "anonymousId");
      anonymousId(anonymousId);

      String userId = segment.userIdCache.get();
      if (!isNullOrEmpty(userId)) {
        //noinspection ConstantConditions
        userId(userId);
      }
    }

    Builder(Message message) {
      type = message.type();
      messageId = message.messageId();
      timestamp = message.timestamp();
      context = message.context();
      integrationsBuilder = new LinkedHashMap<>(message.integrations());
      userId = message.userId();
      anonymousId = message.anonymousId();
      this.segment = null;
      this.handler = null;
    }

    /**
     * The Message ID is a unique identifier for each message. If not provided, one will be
     * generated for you. This ID is typically used for deduping - messages with the same IDs as
     * previous events may be dropped.
     *
     * @see <a href="https://segment.com/docs/spec/common/">Common Fields</a>
     */
    public V messageId(String messageId) {
      this.messageId = assertNotNullOrEmpty(messageId, "messageId");
      postEnqueue();
      return self();
    }

    /**
     * Set a timestamp for the event. By default, the current timestamp is used, but you may
     * override it for historical import.
     * <p>
     * This library will automatically create and attach a timestamp to all events.
     *
     * @see <a href="https://segment.com/docs/spec/common/#-timestamp-">Timestamp</a>
     */
    V timestamp(Date timestamp) {
      this.timestamp = assertNotNull(timestamp, "timestamp");
      postEnqueue();
      return self();
    }

    /**
     * Set a map of information about the state of the device. You can add any custom data to the
     * context dictionary that you'd like to have access to in the raw logs.
     * <p>
     * Some keys in the context dictionary have semantic meaning and will be collected for you
     * automatically, depending on the library you send data from. Some keys, such as location and
     * speed need to be manually entered.
     *
     * @see <a href="https://segment.com/docs/spec/common/#context">Context</a>
     */
    public @NonNull V context(@NonNull Map<String, Object> context) {
      assertNotNull(context, "context");
      this.context = immutableCopyOf(context);
      postEnqueue();
      return self();
    }

    /**
     * Set whether this message is sent to the specified integration or not. 'All' is a special key
     * that applies when no key for a specific integration is found.
     *
     * @see <a href="https://segment.com/docs/spec/common/#integrations">Integrations</a>
     */
    public @NonNull V enableIntegration(@NonNull String key, boolean enable) {
      assertNotNullOrEmpty(key, "key");
      if (integrationsBuilder == null) {
        integrationsBuilder = new LinkedHashMap<>();
      }
      integrationsBuilder.put(key, enable);
      postEnqueue();
      return self();
    }

    /**
     * Pass in some options that will only be used by the target integration. This will implicitly
     * mark the integration as enabled.
     *
     * @see <a href="https://segment.com/docs/spec/common/#integrations">Integrations</a>
     */
    public @NonNull V enableIntegration(@NonNull String key, @NonNull Map<String, Object> options) {
      assertNotNullOrEmpty(key, "key");
      assertNotNull(options, "options");
      if (integrationsBuilder == null) {
        integrationsBuilder = new LinkedHashMap<>();
      }
      integrationsBuilder.put(key, immutableCopyOf(options));
      postEnqueue();
      return self();
    }

    /**
     * The Anonymous ID is a pseudo-unique substitute for a User ID, for cases when you don’t have
     * an absolutely unique identifier.
     * <p>
     * This library maintains a track of the user, and will automatically generate an anonymous ID
     * on the first launch and attach to all messages.
     *
     * @see <a href="https://segment.com/docs/spec/identify/#identities">Identities</a>
     * @see <a href="https://segment.com/docs/spec/identify/#anonymous-id">Anonymous ID</a>
     */
    V anonymousId(@NonNull String anonymousId) {
      this.anonymousId = assertNotNullOrEmpty(anonymousId, "anonymousId");
      postEnqueue();
      return self();
    }

    /**
     * The User ID is a persistent unique identifier for a user (such as a database ID).
     * <p>
     * This library maintains a track of the user, and will automatically attach the user id for
     * all messages after the first {@code analytics.identify(userId)} call.
     *
     * @see <a href="https://segment.com/docs/spec/identify/#identities">Identities</a>
     * @see <a href="https://segment.com/docs/spec/identify/#user-id">User ID</a>
     */
    V userId(@NonNull String userId) {
      this.userId = assertNotNullOrEmpty(userId, "userId");
      postEnqueue();
      return self();
    }

    protected abstract T realBuild(Type type, String messageId, Date timestamp,
        Map<String, Object> context, Map<String, Object> integrations, String userId,
        String anonymousId, Segment segment);

    abstract V self();

    /** Create a {@link Message} instance. */
    @NonNull T build() {
      assertNotNull(type, "type");

      if (isNullOrEmpty(userId) && isNullOrEmpty(anonymousId)) {
        throw new NullPointerException("either userId or anonymousId is required");
      }

      Map<String, Object> integrations = integrationsBuilder == null ? //
          Collections.<String, Object>emptyMap() : immutableCopyOf(integrationsBuilder);

      if (isNullOrEmpty(messageId)) {
        messageId = UUID.randomUUID().toString();
      }

      if (timestamp == null) {
        timestamp = new Date();
      }

      return realBuild(type, messageId, timestamp, context, integrations, userId, anonymousId,
          segment);
    }

    /** Enqueue a {@link Message} instance. Enqueuing is implicit and will be run automatically. */
    public Future<T> enqueue() {
      assertNotNull(segment, "segment");
      assertNotNull(handler, "handler");
      handler.removeCallbacks(enqueueRunnable);
      return segment.enqueue(build());
    }

    void postEnqueue() {
      if (handler == null) {
        return;
      }
      handler.removeCallbacks(enqueueRunnable);
      handler.post(enqueueRunnable);
    }
  }
}