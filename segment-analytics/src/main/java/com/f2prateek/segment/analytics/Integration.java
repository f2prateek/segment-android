package com.f2prateek.segment.analytics;

import android.support.annotation.NonNull;
import com.f2prateek.segment.model.Message;
import java.util.concurrent.Future;

public interface Integration {
  @NonNull Future<Message> enqueue(Message message);

  @NonNull String name();
}
