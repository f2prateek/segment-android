package com.f2prateek.segment.analytics;

import com.f2prateek.segment.model.Message;

/** Intercept every message after it is built to process it further. */
@Beta public interface Interceptor {
  /**
   * Called for every message. This will be called on the same thread the request was made.
   */
  void intercept(Chain chain);

  interface Chain {
    Message message();

    void proceed(Message message);
  }
}
