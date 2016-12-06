package com.f2prateek.segment.android;

import com.f2prateek.segment.model.Message;
import com.f2prateek.segment.model.SegmentMoshiAdapterFactory;
import com.f2prateek.segment.model.TrackMessage;
import com.squareup.tape2.ObjectQueue;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class) //
@Config(constants = BuildConfig.class, sdk = 23) //
public class MoshiMessageConverterTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();
  ObjectQueue<Message> queue;

  @Before public void setUp() throws IOException {
    File parent = folder.getRoot();
    File file = new File(parent, "segment-queue");

    ObjectQueue.Converter<Message> converter =
        new MoshiMessageConverter(SegmentMoshiAdapterFactory.moshi());
    queue = ObjectQueue.create(file, converter);
  }

  @Test public void disallowsLargeMessages() throws IOException {
    Map<String, Object> properties = new LinkedHashMap<>();
    for (int i = 0; i < 374; i++) {
      properties.put("prop_" + i, "abcdefghijklmnopqrstuvwxyz");
    }

    try {
      queue.add(new TrackMessage.Builder().userId("userId")
          .event("event")
          .properties(properties)
          .build());
      fail();
    } catch (MoshiMessageConverter.MessageTooLargeException ignored) {
    }
  }
}
