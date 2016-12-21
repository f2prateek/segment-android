package com.f2prateek.segment.android;

import android.util.JsonWriter;
import com.f2prateek.segment.model.Batch;
import com.f2prateek.segment.model.JsonAdapter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;

final class MessageRetrofitConverter implements Converter<Batch, RequestBody> {
  private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

  @Override public RequestBody convert(Batch batch) throws IOException {
    Buffer buffer = new Buffer();
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(buffer.outputStream()));
    JsonAdapter.toJson(writer, batch);
    writer.close();
    return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
  }
}
