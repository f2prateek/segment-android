package com.f2prateek.segment.model;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static com.f2prateek.segment.model.Utils.assertNotNull;
import static com.f2prateek.segment.model.Utils.assertNotNullOrEmpty;
import static com.f2prateek.segment.model.Utils.immutableCopyOf;
import static com.f2prateek.segment.model.Utils.isNullOrEmpty;

/** Encapsulates properties common to all messages. */
public abstract class Message {
  public enum Type {
    identify, group, track, screen, alias
  }

  final Type type;
  final String messageId;
  final Date timestamp;
  final Map<String, Object> context;
  final Map<String, Object> integrations;
  final String userId;
  final String anonymousId;

  Message(Type type, String messageId, Date timestamp, Map<String, Object> context,
      Map<String, Object> integrations, String userId, String anonymousId) {
    this.type = type;
    this.messageId = messageId;
    this.timestamp = timestamp;
    this.context = context;
    this.integrations = integrations;
    this.userId = userId;
    this.anonymousId = anonymousId;
  }

  public final @NonNull Type type() {
    return type;
  }

  public final @Nullable String messageId() {
    return messageId;
  }

  public final @Nullable Date timestamp() {
    return timestamp;
  }

  public final @Nullable Map<String, Object> context() {
    return context;
  }

  public final @Nullable Map<String, Object> integrations() {
    return integrations;
  }

  public final @Nullable String userId() {
    return userId;
  }

  public final @Nullable String anonymousId() {
    return anonymousId;
  }

  public abstract @NonNull Builder toBuilder();

  /** Fluent API to construct instances of a {@link Message} of type {@link T}. */
  public static abstract class Builder<T extends Message, V extends Builder> {
    private @NonNull final Message.Type type;
    private String messageId;
    private Date timestamp;
    private Map<String, Object> context;
    private Map<String, Object> integrationsBuilder;
    private String userId;
    private String anonymousId;

    Builder(@NonNull Type type) {
      this.type = type;
    }

    Builder(Message message) {
      type = message.type();
      messageId = message.messageId();
      timestamp = message.timestamp();
      context = message.context();
      integrationsBuilder = new LinkedHashMap<>(message.integrations());
      userId = message.userId();
      anonymousId = message.anonymousId();
    }

    /**
     * The Message ID is a unique identifier for each message. If not provided, one will be
     * generated for you. This ID is typically used for deduping - messages with the same IDs as
     * previous events may be dropped.
     *
     * @see <a href="https://segment.com/docs/spec/common/">Common Fields</a>
     */
    public @NonNull V messageId(String messageId) {
      this.messageId = assertNotNullOrEmpty(messageId, "messageId");
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
    public @NonNull V timestamp(Date timestamp) {
      this.timestamp = assertNotNull(timestamp, "timestamp");
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
      return self();
    }

    /**
     * Set whether this message is sent to the specified integration or not. 'All' is a special key
     * that applies when no key for a specific integration is found.
     *
     * @see <a href="https://segment.com/docs/spec/common/#integrations">Integrations</a>
     */
    public @NonNull V integration(@NonNull String key, boolean enable) {
      assertNotNullOrEmpty(key, "key");
      if (integrationsBuilder == null) {
        integrationsBuilder = new LinkedHashMap<>();
      }
      integrationsBuilder.put(key, enable);
      return self();
    }

    /**
     * Pass in some options that will only be used by the target integration. This will implicitly
     * mark the integration as enabled.
     *
     * @see <a href="https://segment.com/docs/spec/common/#integrations">Integrations</a>
     */
    public @NonNull V integration(@NonNull String key, @NonNull Map<String, Object> options) {
      assertNotNullOrEmpty(key, "key");
      assertNotNull(options, "options");
      if (integrationsBuilder == null) {
        integrationsBuilder = new LinkedHashMap<>();
      }
      integrationsBuilder.put(key, immutableCopyOf(options));
      return self();
    }

    /**
     * The Anonymous ID is a pseudo-unique substitute for a User ID, for cases when you don’t have
     * an absolutely unique identifier.
     *
     * @see <a href="https://segment.com/docs/spec/identify/#identities">Identities</a>
     * @see <a href="https://segment.com/docs/spec/identify/#anonymous-id">Anonymous ID</a>
     */
    public @NonNull V anonymousId(@NonNull String anonymousId) {
      this.anonymousId = assertNotNullOrEmpty(anonymousId, "anonymousId");
      return self();
    }

    /**
     * The User ID is a persistent unique identifier for a user (such as a database ID).
     *
     * @see <a href="https://segment.com/docs/spec/identify/#identities">Identities</a>
     * @see <a href="https://segment.com/docs/spec/identify/#user-id">User ID</a>
     */
    public @NonNull V userId(@NonNull String userId) {
      this.userId = assertNotNullOrEmpty(userId, "userId");
      return self();
    }

    protected abstract T realBuild(Type type, String messageId, Date timestamp,
        Map<String, Object> context, Map<String, Object> integrations, String userId,
        String anonymousId);

    abstract V self();

    /** Create a {@link Message} instance. */
    @CheckResult public @NonNull T build() {
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

      return realBuild(type, messageId, timestamp, context, integrations, userId, anonymousId);
    }
  }
}