package com.f2prateek.segment.android;

import com.f2prateek.segment.model.Message;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.tape2.ObjectQueue;
import java.io.IOException;
import java.io.OutputStream;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;

/**
 * A {@link Moshi} backed {@link ObjectQueue.Converter} implementation.
 * This is also adds behaviour specific to Segment — namely that it disallows saving messages whose
 * Json representation is greater than {@link MoshiMessageConverter#MAX_MESSAGE_SIZE}.
 */
final class MoshiMessageConverter implements ObjectQueue.Converter<Message> {
  // Single message is limited to 15kb.
  static final int MAX_MESSAGE_SIZE = 15000;

  private final JsonAdapter<Message> adapter;

  MoshiMessageConverter(Moshi moshi) {
    this.adapter = moshi.adapter(Message.class);
  }

  @Override public Message from(byte[] bytes) throws IOException {
    Buffer buffer = new Buffer();
    buffer.write(bytes);
    return adapter.fromJson(buffer);
  }

  @Override public void toStream(Message m, OutputStream os) throws IOException {
    Buffer buffer = new Buffer();
    adapter.toJson(buffer, m);

    long size = buffer.size();
    if (size >= MAX_MESSAGE_SIZE) {
      String json = buffer.snapshot().toString();
      throw new MessageTooLargeException(json, m);
    }

    BufferedSink sink = Okio.buffer(Okio.sink(os));
    sink.writeAll(buffer);
    sink.close();
  }

  static final class MessageTooLargeException extends IOException {
    final String json;
    final Message message;

    MessageTooLargeException(String json, Message message) {
      super("Json must be less than " + MAX_MESSAGE_SIZE);
      this.json = json;
      this.message = message;
    }

    @Override public String toString() {
      return "MessageTooLargeException{" +
          "json='" + json + '\'' +
          ", message=" + message +
          '}';
    }
  }
}
