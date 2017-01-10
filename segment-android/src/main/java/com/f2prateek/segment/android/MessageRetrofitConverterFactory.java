package com.f2prateek.segment.android;

import com.f2prateek.segment.model.Batch;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.RequestBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

final class MessageRetrofitConverterFactory extends Converter.Factory {
  @Override public Converter<?, RequestBody> requestBodyConverter(Type type,
      Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
    if (type != Batch.class) return null;
    return new MessageRetrofitConverter();
  }
}
