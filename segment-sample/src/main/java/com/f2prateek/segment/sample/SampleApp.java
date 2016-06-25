package com.f2prateek.segment.sample;

import android.app.Application;
import com.f2prateek.segment.Segment;
import java.util.LinkedHashMap;
import java.util.Map;

public class SampleApp extends Application {
  @Override public void onCreate() {
    super.onCreate();

    Segment segment = new Segment.Builder().writeKey("5m6gbdgho6").context(this).build();
    Segment.setSingletonInstance(segment);

    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put("foo", "bar");
    Segment.with(this).track("Event A").properties(properties);
  }
}
