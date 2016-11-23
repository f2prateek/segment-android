package com.f2prateek.segment.sample;

import android.app.Application;
import android.support.annotation.Nullable;
import com.f2prateek.segment.Interceptor;
import com.f2prateek.segment.Message;
import com.f2prateek.segment.Segment;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class SampleApp extends Application {
  @Override public void onCreate() {
    super.onCreate();

    //noinspection SpellCheckingInspection
    Segment segment = new Segment.Builder() //
        .writeKey("5m6gbdgho6") //
        .context(this) //
        .interceptor(new Interceptor() {
          @Nullable @Override public Future<Message> intercept(Chain chain) {
            Message message = chain.message();
            return chain.proceed(message.toBuilder().integration("Amplitude", false).build());
          }
        }) //
        .build();

    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put("foo", "bar");
    segment.enqueue(segment.newTrack("Event A").properties(properties).build());
  }
}
