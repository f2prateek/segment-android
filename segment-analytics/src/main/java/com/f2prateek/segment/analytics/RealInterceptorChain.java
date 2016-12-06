package com.f2prateek.segment.analytics;

import com.f2prateek.segment.model.Message;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.f2prateek.segment.analytics.Utils.isNullOrEmpty;

class RealInterceptorChain implements Interceptor.Chain {
  int index;
  final Message message;
  final List<Interceptor> interceptors;
  final List<Integration> integrations;
  final ExecutorService executorService;

  RealInterceptorChain(int index, Message message, List<Interceptor> interceptors,
      List<Integration> integrations) {
    this.index = index;
    this.message = message;
    this.interceptors = interceptors;
    this.integrations = integrations;
    executorService = new ThreadPoolExecutor(0, integrations.size(), 30L, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>());
  }

  @Override public Message message() {
    return message;
  }

  @Override public Future<Message> proceed(final Message message) {
    // If there's another interceptor in the chain, call that.
    if (index < interceptors.size()) {
      Interceptor.Chain chain =
          new RealInterceptorChain(index + 1, message, interceptors, integrations);
      return interceptors.get(index).intercept(chain);
    }

    // No more interceptors. Send to integrations.
    return executorService.submit(new Callable<Message>() {
      @Override public Message call() throws Exception {
        Map<String, Object> options = message.integrations();

        // Set what the default option is. This is true, unless an explicit value has been provided
        // for the key "All".
        boolean defaultOption = true;
        if (isNullOrEmpty(options)) {
          defaultOption = true;
        } else {
          Object allOptions = options.get("All");
          if (allOptions instanceof Boolean) {
            defaultOption = (Boolean) allOptions;
          }
        }

        for (Integration integration : integrations) {
          // If there are no integration specific options, use the defaults.
          if (isNullOrEmpty(options)) {
            if (defaultOption) {
              integration.enqueue(message);
            }
            continue;
          }

          Object integrationOptions = options.get(integration.name());

          // If the options provided are not a boolean, send the event.
          if (!(integrationOptions instanceof Boolean)) {
            integration.enqueue(message);
            continue;
          }

          // If the options provided are a boolean, use the event.
          boolean enabled = (Boolean) integrationOptions;
          if (enabled) {
            integration.enqueue(message);
          }
        }
        return message;
      }
    });
  }
}