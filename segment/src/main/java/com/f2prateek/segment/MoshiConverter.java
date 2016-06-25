package com.f2prateek.segment;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.tape2.ObjectQueue;
import java.io.IOException;
import java.io.OutputStream;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;

/** A {@link Moshi} based {@link ObjectQueue.Converter} implementation. */
final class MoshiConverter<T> implements ObjectQueue.Converter<T> {
  private final JsonAdapter<T> adapter;

  MoshiConverter(Moshi moshi, Class<T> clazz) {
    this.adapter = moshi.adapter(clazz);
  }

  @Override public T from(byte[] bytes) throws IOException {
    Buffer buffer = new Buffer();
    buffer.write(bytes);
    return adapter.fromJson(buffer);
  }

  @Override public void toStream(T o, OutputStream os) throws IOException {
    BufferedSink sink = Okio.buffer(Okio.sink(os));
    adapter.toJson(sink, o);
    sink.close();
  }
}
