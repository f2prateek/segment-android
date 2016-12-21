package com.f2prateek.segment.android;

import android.util.JsonReader;
import android.util.JsonWriter;
import com.f2prateek.segment.model.Message;
import com.squareup.tape2.ObjectQueue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

final class MessageObjectQueueConverter implements ObjectQueue.Converter<Message> {
  @Override public Message from(byte[] bytes) throws IOException {
    InputStream is = new ByteArrayInputStream(bytes);
    JsonReader reader = new JsonReader(new InputStreamReader(is));
    return JsonUtils.fromJson(reader);
  }

  @Override public void toStream(Message m, OutputStream bytes) throws IOException {
    CountingOutputStream countingOutputStream = new CountingOutputStream(bytes);
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(countingOutputStream));
    JsonUtils.toJson(writer, m);
    writer.close();

    long count = countingOutputStream.getCount();
    if (count > JsonUtils.MAX_MESSAGE_SIZE) {
      throw new JsonUtils.MessageTooLargeException(m, count);
    }
  }
}
