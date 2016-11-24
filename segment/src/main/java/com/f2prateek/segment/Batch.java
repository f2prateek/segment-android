package com.f2prateek.segment;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Date;
import java.util.List;

@AutoValue //
abstract class Batch {
  static Batch create(List<Message> batch) {
    return new AutoValue_Batch(batch, new Date());
  }

  abstract List<Message> batch();

  abstract Date sentAt();

  public static JsonAdapter<Batch> jsonAdapter(Moshi moshi) {
    return new AutoValue_Batch.MoshiJsonAdapter(moshi);
  }
}
