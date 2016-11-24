package com.f2prateek.segment.sample;

import android.app.Application;
import com.f2prateek.segment.ContextInterceptor;
import com.f2prateek.segment.Segment;
import java.util.LinkedHashMap;
import java.util.Map;

public class SampleApp extends Application {
  @Override public void onCreate() {
    super.onCreate();

    //noinspection SpellCheckingInspection
    Segment segment = new Segment.Builder() //
        .writeKey("5m6gbdgho6") //
        .context(this) //
        .interceptor(ContextInterceptor.with(this)) //
        .build();

    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put("foo", "bar");
    segment.enqueue(segment.newTrack("Event A").properties(properties).build());
    segment.flush();
  }
}
