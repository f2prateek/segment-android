package com.f2prateek.segment;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@AutoValue //
abstract class Batch {
  private static final AtomicInteger SEQUENCE_GENERATOR = new AtomicInteger();

  static Batch create(Map<String, Object> context, List<Message> batch) {
    return new AutoValue_Batch(batch, new Date(), context, SEQUENCE_GENERATOR.incrementAndGet());
  }

  abstract List<Message> batch();

  abstract Date sentAt();

  abstract Map<String, Object> context();

  abstract int sequence();

  public static JsonAdapter<Batch> jsonAdapter(Moshi moshi) {
    return new AutoValue_Batch.MoshiJsonAdapter(moshi);
  }
}
