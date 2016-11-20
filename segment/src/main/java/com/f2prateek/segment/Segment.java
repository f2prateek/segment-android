package com.f2prateek.segment;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Rfc3339DateJsonAdapter;
import com.squareup.tape2.ObjectQueue;
import com.squareup.tape2.QueueFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import static com.f2prateek.segment.Utils.assertNotNull;
import static com.f2prateek.segment.Utils.assertNotNullOrEmpty;
import static com.f2prateek.segment.Utils.hasPermission;
import static com.f2prateek.segment.Utils.isNullOrEmpty;

public final class Segment {
  private final List<Interceptor> interceptors;
  private final Transporter transporter;
  private final StringCache userIdCache;
  private final StringCache anonymousIdCache;

  @Private Segment(List<Interceptor> interceptors, Transporter transporter, StringCache userIdCache,
      StringCache anonymousIdCache) {
    this.interceptors = interceptors;
    this.transporter = transporter;
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
  public @NonNull AliasMessage.Builder newAlias(@NonNull String newId) {
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
  public @NonNull GroupMessage.Builder newGroup(@NonNull String groupId) {
    return lift(new GroupMessage.Builder()).groupId(groupId);
  }

  /**
   * Identify lets you tie one of your users and their actions to a recognizable {@code userId}. It
   * also lets you record {@code traits} about the user, like their email, name, account type, etc.
   *
   * @param userId Unique identifier which you recognize a user by in your own database.
   * @see <a href="https://segment.com/docs/tracking-api/identify/">Identify Documentation</a>
   */
  public @NonNull IdentifyMessage.Builder newIdentify(@NonNull String userId) {
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
  public @NonNull IdentifyMessage.Builder newIdentify(@NonNull Map<String, Object> traits) {
    return lift(new IdentifyMessage.Builder()).traits(traits);
  }

  /**
   * The screen methods let your record whenever a user sees a screen of your mobile app, and
   * attach a name and properties to the screen.
   *
   * @param name A name for the screen
   * @see <a href="http://segment.com/docs/tracking-api/page-and-screen/">Screen Documentation</a>
   */
  public @NonNull ScreenMessage.Builder newScreen(@NonNull String name) {
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
  public @NonNull TrackMessage.Builder newTrack(@NonNull String event) {
    return lift(new TrackMessage.Builder()).event(event);
  }

  /**
   * Enqueue a {@link Message} to be uploaded at a later time and returns a Future whose {@link
   * Future#get()} method blocks until the event is queued on disk.
   */
  public @Nullable Future<Message> enqueue(Message message) {
    Message m = message;
    for (Interceptor interceptor : interceptors) {
      m = interceptor.intercept(m);
      if (m == null) {
        return null;
      }
    }
    return transporter.enqueue(m);
  }

  /**
   * Resets the analytics client by clearing any stored information about the user. Events queued
   * on disk are not cleared, and will be uploaded at a later time.
   */
  public void reset() {
    userIdCache.clear();
    anonymousIdCache.set(UUID.randomUUID().toString());
  }

  /**
   * Flushes all messages in the queue to the server, and returns a Future whose {@link
   * Future#get()} method blocks until completion.
   */
  public @NonNull Future<List<Message>> flush() {
    return transporter.flush();
  }

  public static class Builder {
    private static final HttpUrl DEFAULT_BASE_URL = HttpUrl.parse("https://api.segment.io");

    String writeKey;
    Context context;
    List<Interceptor> interceptors;
    OkHttpClient client;
    HttpUrl baseUrl;
    ObjectQueue<Message> queue;

    /**
     * Provide the context to be used by the client. The context must declare that it uses the
     * {@link Manifest.permission#INTERNET} permission.
     */
    public @NonNull Builder context(@NonNull Context context) {
      assertNotNull(context, "context");
      if (!hasPermission(context, Manifest.permission.INTERNET)) {
        throw new IllegalArgumentException("INTERNET permission is required.");
      }
      this.context = context;
      return this;
    }

    /**
     * Provide the writeKey for the client for the Segment project the client should upload events
     * to.
     */
    public @NonNull Builder writeKey(@NonNull String writeKey) {
      this.writeKey = assertNotNullOrEmpty(writeKey, "writeKey");
      return this;
    }

    /** Add a {@link Interceptor} for intercepting messages. */
    public @NonNull Builder interceptor(@NonNull Interceptor interceptor) {
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

    /**
     * Set the HTTP client to be used for network requests.
     */
    public @NonNull Builder client(@NonNull OkHttpClient client) {
      this.client = assertNotNull(client, "client");
      return this;
    }

    /**
     * Provide the queue used by the client. A {@link QueueFile} backed implementation is used by
     * default.
     */
    public @NonNull Builder queue(@NonNull ObjectQueue<Message> queue) {
      this.queue = assertNotNull(queue, "queue");
      return this;
    }

    /**
     * Set a base Url that this client should upload events to. Uses {@code https://api.segment.io}
     * by default.
     */
    public Builder baseUrl(String url) {
      assertNotNullOrEmpty(url, "url");
      HttpUrl baseUrl = HttpUrl.parse(url);
      if (baseUrl == null) {
        throw new AssertionError("url is invalid");
      }
      this.baseUrl = baseUrl;
      return this;
    }

    /**
     * Set a base Url that this client should upload events to. Uses {@code https://api.segment.io}
     * by default.
     */
    public Builder baseUrl(HttpUrl baseUrl) {
      this.baseUrl = assertNotNull(baseUrl, "baseUrl");
      return this;
    }

    @NonNull public Segment build() {
      assertNotNull(context, "context");
      assertNotNull(writeKey, "writeKey");

      List<Interceptor> interceptors;
      if (this.interceptors == null) {
        interceptors = Collections.emptyList();
      } else {
        interceptors = Collections.unmodifiableList(this.interceptors);
      }

      okhttp3.Interceptor authInterceptor = new okhttp3.Interceptor() {
        public okhttp3.Response intercept(Chain chain) throws IOException {
          okhttp3.Request request = chain.request()
              .newBuilder()
              .addHeader("Authorization", Credentials.basic(writeKey, ""))
              .build();
          return chain.proceed(request);
        }
      };

      OkHttpClient client = this.client;
      if (client == null) {
        client = new OkHttpClient.Builder().addInterceptor(authInterceptor).build();
      } else {
        client = client.newBuilder().addInterceptor(authInterceptor).build();
      }

      HttpUrl baseUrl = this.baseUrl;
      if (baseUrl == null) {
        baseUrl = DEFAULT_BASE_URL;
      }

      final Moshi moshi = new Moshi.Builder() //
          .add(SegmentMoshiAdapterFactory.create()) //
          .add(Date.class, new Rfc3339DateJsonAdapter()) //
          .build();

      ObjectQueue<Message> queue = this.queue;
      if (queue == null) {
        try {
          File directory = context.getDir("segment-queue", Context.MODE_PRIVATE);
          File file = new File(directory, writeKey.hashCode() + ".segment");
          MoshiConverter<Message> converter = new MoshiConverter<>(moshi, Message.class);
          queue = ObjectQueue.create(file, converter);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      Retrofit retrofit = new Retrofit.Builder().client(client) //
          .baseUrl(baseUrl) //
          .addConverterFactory(MoshiConverterFactory.create(moshi)) //
          .build();

      TrackingAPI trackingAPI = retrofit.create(TrackingAPI.class);

      Transporter transporter = new Transporter(queue, trackingAPI);

      SharedPreferences sharedPreferences =
          context.getSharedPreferences("segment_" + writeKey.hashCode(), Context.MODE_PRIVATE);
      StringCache userIdCache = new StringCache(sharedPreferences, "userId");
      StringCache anonymousIdCache = new StringCache(sharedPreferences, "anonymousId");
      if (isNullOrEmpty(anonymousIdCache.get())) {
        anonymousIdCache.set(UUID.randomUUID().toString());
      }

      return new Segment(interceptors, transporter, userIdCache, anonymousIdCache);
    }
  }
}
