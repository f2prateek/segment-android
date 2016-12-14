package com.f2prateek.segment.android;

import com.f2prateek.segment.model.Message;
import java.util.List;
import java.util.concurrent.Future;

class RealInterceptorChain implements Interceptor.Chain {
  private final int index;
  final Message message;
  private final List<Interceptor> interceptors;
  final Transporter transporter;

  RealInterceptorChain(int index, Message message, List<Interceptor> interceptors,
      Transporter transporter) {
    this.index = index;
    this.message = message;
    this.interceptors = interceptors;
    this.transporter = transporter;
  }

  @Override public Message message() {
    return message;
  }

  @Override public Future<Message> proceed(Message message) {
    // If there's another interceptor in the chain, call that.
    if (index < interceptors.size()) {
      Interceptor.Chain chain =
          new RealInterceptorChain(index + 1, message, interceptors, transporter);
      return interceptors.get(index).intercept(chain);
    }

    // No more interceptors. Transport.
    return transporter.enqueue(message);
  }
}