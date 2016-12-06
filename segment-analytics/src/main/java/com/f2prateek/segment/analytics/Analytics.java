package com.f2prateek.segment.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.f2prateek.segment.model.AliasMessage;
import com.f2prateek.segment.model.GroupMessage;
import com.f2prateek.segment.model.IdentifyMessage;
import com.f2prateek.segment.model.Message;
import com.f2prateek.segment.model.ScreenMessage;
import com.f2prateek.segment.model.TrackMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import static android.content.Context.MODE_PRIVATE;
import static com.f2prateek.segment.analytics.Utils.assertNotNull;
import static com.f2prateek.segment.analytics.Utils.assertNotNullOrEmpty;
import static com.f2prateek.segment.analytics.Utils.isNullOrEmpty;

public final class Analytics {
  private final List<Interceptor> interceptors;
  private final List<Integration> integrations;
  private final StringCache userIdCache;
  private final StringCache anonymousIdCache;

  Analytics(List<Interceptor> interceptors, List<Integration> integrations, StringCache userIdCache,
      StringCache anonymousIdCache) {
    this.interceptors = interceptors;
    this.integrations = integrations;
    this.userIdCache = userIdCache;
    this.anonymousIdCache = anonymousIdCache;
  }

  private <T extends Message, V extends Message.Builder> V lift(Message.Builder<T, V> builder) {
    String anonymousId = assertNotNullOrEmpty(anonymousIdCache.get(), "anonymousId");
    builder.anonymousId(anonymousId);

    String userId = userIdCache.get();
    if (!isNullOrEmpty(userId)) {
      builder.userId(userId);
    }

    //noinspection unchecked
    return (V) builder;
  }

  /**
   * The alias method is used to merge two user identities, effectively connecting two sets of user
   * data as one. This is an advanced method, but it is required to manage user identities
   * successfully in some of our integrations.
   * <p>
   *
   * Usage:
   * <pre> <code>
   *   segment.enqueue(segment.newTrack("Home Button Clicked"));
   *   segment.enqueue(segment.newAlias(newUserId));
   *   segment.enqueue(segment.newIdentify(newUserId));
   * </code> </pre>
   *
   * @param newId The new ID you want to alias the existing ID to. The existing ID will be either
   * the previousId if you have called identify, or the anonymous ID.
   * @see <a href="https://segment.com/docs/tracking-api/alias/">Alias Documentation</a>
   */
  @CheckResult public @NonNull AliasMessage.Builder newAlias(@NonNull String newId) {
    String previousId = userIdCache.get();
    if (isNullOrEmpty(previousId)) {
      previousId = assertNotNullOrEmpty(anonymousIdCache.get(), "anonymousId");
    }
    return lift(new AliasMessage.Builder()).previousId(previousId).userId(newId);
  }

  /**
   * The group method lets you associate a user with a group. It also lets you record custom traits
   * about the group, like industry or number of employees.
   *
   * @param groupId Unique identifier which you recognize a group by in your own database. Must not
   * be null or empty.
   * @see <a href="https://segment.com/docs/tracking-api/group/">Group Documentation</a>
   */
  @CheckResult public @NonNull GroupMessage.Builder newGroup(@NonNull String groupId) {
    return lift(new GroupMessage.Builder()).groupId(groupId);
  }

  /**
   * Identify lets you tie one of your users and their actions to a recognizable {@code userId}. It
   * also lets you record {@code traits} about the user, like their email, name, account type, etc.
   *
   * @param userId Unique identifier which you recognize a user by in your own database.
   * @see <a href="https://segment.com/docs/tracking-api/identify/">Identify Documentation</a>
   */
  @CheckResult public @NonNull IdentifyMessage.Builder newIdentify(@NonNull String userId) {
    userIdCache.set(userId);
    return lift(new IdentifyMessage.Builder());
  }

  /**
   * Identify lets you tie one of your users and their actions to a recognizable {@code userId}. It
   * also lets you record {@code traits} about the user, like their email, name, account type, etc.
   *
   * @param traits Traits about the user
   * @see <a href="https://segment.com/docs/tracking-api/identify/">Identify Documentation</a>
   */
  @CheckResult public @NonNull IdentifyMessage.Builder newIdentify(
      @NonNull Map<String, Object> traits) {
    return lift(new IdentifyMessage.Builder()).traits(traits);
  }

  /**
   * The screen methods let your record whenever a user sees a screen of your mobile app, and
   * attach a name and properties to the screen.
   *
   * @param name A name for the screen
   * @see <a href="http://segment.com/docs/tracking-api/page-and-screen/">Screen Documentation</a>
   */
  @CheckResult public @NonNull ScreenMessage.Builder newScreen(@NonNull String name) {
    return lift(new ScreenMessage.Builder()).name(name);
  }

  /**
   * The track method is how you record any actions your users perform. Each action is known by a
   * name, like 'Purchased a T-Shirt'. You can also record properties specific to those actions.
   * For example a 'Purchased a Shirt' event might have properties like revenue or size.
   *
   * @param event Name of the event. Must not be null or empty.
   * @see <a href="https://segment.com/docs/tracking-api/track/">Track Documentation</a>
   */
  @CheckResult public @NonNull TrackMessage.Builder newTrack(@NonNull String event) {
    return lift(new TrackMessage.Builder()).event(event);
  }

  /**
   * Enqueue a {@link Message} to be uploaded at a later time and
   * returns a Future. By default, the
   * Future's {@link Future#get()} method blocks until the event is queued on disk, but
   * interceptors may change this behaviour.
   */
  public @Nullable Future<Message> enqueue(Message message) {
    Interceptor.Chain chain = new RealInterceptorChain(0, message, interceptors, integrations);
    return chain.proceed(message);
  }

  /**
   * Resets the analytics client by clearing any stored information about the user. Events queued
   * on disk are not cleared, and will be uploaded at a later time.
   */
  public void reset() {
    userIdCache.clear();
    anonymousIdCache.set(UUID.randomUUID().toString());
  }

  public static class Builder {
    Context context;
    List<Interceptor> interceptors;
    List<Integration> integrations;

    /** Provide the context to be used by the client. */
    @CheckResult public @NonNull Builder context(@NonNull Context context) {
      assertNotNull(context, "context");
      this.context = context;
      return this;
    }

    /** Add a {@link Interceptor} for intercepting messages. */
    @CheckResult public @NonNull Builder interceptor(@NonNull Interceptor interceptor) {
      assertNotNull(interceptor, "interceptor");
      if (interceptors == null) {
        interceptors = new ArrayList<>();
      }
      if (interceptors.contains(interceptor)) {
        throw new IllegalStateException("Interceptor is already registered.");
      }
      interceptors.add(interceptor);
      return this;
    }

    /** Add a {@link Integration} for accepting messages. */
    @CheckResult public @NonNull Builder integration(@NonNull Integration integration) {
      assertNotNull(integration, "integration");
      if (integrations == null) {
        integrations = new ArrayList<>();
      }
      if (integrations.contains(integration)) {
        throw new IllegalStateException("Integration is already registered.");
      }
      integrations.add(integration);
      return this;
    }

    @CheckResult public @NonNull Analytics build() {
      assertNotNull(context, "context");

      List<Interceptor> interceptors;
      if (this.interceptors == null) {
        interceptors = Collections.emptyList();
      } else {
        interceptors = Collections.unmodifiableList(this.interceptors);
      }

      List<Integration> integrations;
      if (this.integrations == null) {
        integrations = Collections.emptyList();
      } else {
        integrations = Collections.unmodifiableList(this.integrations);
      }

      SharedPreferences sharedPreferences =
          context.getSharedPreferences("segment_analytics", MODE_PRIVATE);
      StringCache userIdCache = new StringCache(sharedPreferences, "userId");
      StringCache anonymousIdCache = new StringCache(sharedPreferences, "anonymousId");
      if (isNullOrEmpty(anonymousIdCache.get())) {
        anonymousIdCache.set(UUID.randomUUID().toString());
      }

      return new Analytics(interceptors, integrations, userIdCache, anonymousIdCache);
    }
  }
}
