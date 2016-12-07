package com.f2prateek.segment.android;

import android.support.annotation.Nullable;
import com.f2prateek.segment.model.Message;
import java.util.concurrent.Future;

/** Intercept every message after it is built to process it further. */
@Beta public interface Interceptor {
  /**
   * Called for every message. This will be called on the same thread the request was made.
   */
  @Nullable Future<Message> intercept(Chain chain);

  @Beta interface Chain {
    Message message();

    Future<Message> proceed(Message message);
  }
}
