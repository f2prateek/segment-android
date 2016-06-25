package com.f2prateek.segment;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Rfc3339DateJsonAdapter;
import com.squareup.tape2.ObjectQueue;
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
import static com.f2prateek.segment.Utils.getStringResource;
import static com.f2prateek.segment.Utils.hasPermission;
import static com.f2prateek.segment.Utils.isNullOrEmpty;

public class Segment {
  volatile static Segment singleton = null;

  /**
   * Set the global instance returned from {@link #with}.
   * <p/>
   * This method must be called before any calls to {@link #with} and may only be called once.
   */
  public static void setSingletonInstance(Segment segment) {
    synchronized (Segment.class) {
      if (singleton != null) {
        throw new IllegalStateException("Singleton instance already exists.");
      }
      singleton = segment;
    }
  }

  /**
   * Return a reference to the global default {@link Segment} instance.
   * <p/>
   * This instance is automatically initialized with defaults that are suitable to most
   * implementations. You must provide a {@code segment_write_key} string resource.
   * <p/>
   * If these settings do not meet the requirements of your application, you can construct your own
   * instance with full control over the configuration by using {@link Builder}.
   * <p/>
   * By default, events are uploaded every 30 seconds, or every 20 events (whichever occurs first),
   * and debugging is disabled.
   */
  public static @NonNull Segment with(@NonNull Context context) {
    if (singleton == null) {
      synchronized (Segment.class) {
        if (singleton == null) {
          assertNotNull(context, "context == null");
          String writeKey = getStringResource(context, "segment_write_key");
          assertNotNull(writeKey, "writeKey == null");
          singleton = new Builder().context(context).writeKey(writeKey).build();
        }
      }
    }
    return singleton;
  }

  private final List<Interceptor> interceptors;
  private final Transporter transporter;
  final StringCache userIdCache;
  final StringCache anonymousIdCache;

  @Private Segment(List<Interceptor> interceptors, Transporter transporter, StringCache userIdCache,
      StringCache anonymousIdCache) {
    this.interceptors = interceptors;
    this.transporter = transporter;
    this.userIdCache = userIdCache;
    this.anonymousIdCache = anonymousIdCache;
  }

  public @NonNull AliasMessage.Builder alias(@NonNull String userId) {
    return new AliasMessage.Builder(this).userId(userId);
  }

  public @NonNull GroupMessage.Builder group(@NonNull String groupId) {
    return new GroupMessage.Builder(this).groupId(groupId);
  }

  public @NonNull IdentifyMessage.Builder identify(@NonNull String userId) {
    return new IdentifyMessage.Builder(this).userId(userId);
  }

  public @NonNull IdentifyMessage.Builder identify(@NonNull Map<String, Object> traits) {
    return new IdentifyMessage.Builder(this).traits(traits);
  }

  public @NonNull ScreenMessage.Builder screen(@NonNull String name) {
    return new ScreenMessage.Builder(this).name(name);
  }

  public @NonNull TrackMessage.Builder track(@NonNull String event) {
    return new TrackMessage.Builder(this).event(event);
  }

  @Nullable <T extends Message> Future<T> enqueue(@NonNull T message) {
    Message m = message;
    for (Interceptor interceptor : interceptors) {
      m = interceptor.intercept(m);
      if (m == null) {
        return null;
      }
    }
    return transporter.enqueue(message);
  }

  @NonNull public Future<List<Message>> flush() {
    return transporter.flush();
  }

  public static class Builder {
    private static final HttpUrl DEFAULT_BASE_URL = HttpUrl.parse("https://api.segment.io");

    String writeKey;
    Context context;
    List<Interceptor> interceptors;
    OkHttpClient client;
    private HttpUrl baseUrl;

    public @NonNull Builder writeKey(@NonNull String writeKey) {
      this.writeKey = assertNotNullOrEmpty(writeKey, "writeKey");
      return this;
    }

    public @NonNull Builder context(@NonNull Context context) {
      assertNotNull(context, "context");
      if (!hasPermission(context, Manifest.permission.INTERNET)) {
        throw new IllegalArgumentException("INTERNET permission is required.");
      }
      this.context = context;
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

    public @NonNull Builder client(@NonNull OkHttpClient client) {
      this.client = assertNotNull(client, "client");
      return this;
    }

    /**
     * Set a base Url that this client should upload events to. Uses {@code https://api.segment.io}
     * by default.
     */
    public Builder baseUrl(String url) {
      assertNotNullOrEmpty(writeKey, "url");
      HttpUrl baseUrl = HttpUrl.parse(url);
      if (baseUrl == null) {
        throw new AssertionError("url is invalid");
      }
      this.baseUrl = baseUrl;
      return this;
    }

    @NonNull public Segment build() {
      List<Interceptor> interceptors;
      if (this.interceptors == null) {
        interceptors = Collections.emptyList();
      } else {
        interceptors = Collections.unmodifiableList(this.interceptors);
      }

      okhttp3.Interceptor authInterceptor = new okhttp3.Interceptor() {
        @Override public okhttp3.Response intercept(Chain chain) throws IOException {
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

      ObjectQueue<Message> messageQueue;
      try {
        File directory = context.getDir("segment-queue", Context.MODE_PRIVATE);
        File file = new File(directory, writeKey.hashCode() + ".segment");
        MoshiConverter<Message> converter = new MoshiConverter<>(moshi, Message.class);
        messageQueue = ObjectQueue.create(file, converter);
      } catch (IOException e) {
        // messageQueue = ObjectQueue.createInMemory();
        throw new RuntimeException(e);
      }

      Retrofit retrofit = new Retrofit.Builder().client(client) //
          .baseUrl(baseUrl) //
          .addConverterFactory(MoshiConverterFactory.create(moshi)) //
          .build();

      TrackingAPI trackingAPI = retrofit.create(TrackingAPI.class);

      Transporter transporter = new Transporter(messageQueue, trackingAPI);

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
