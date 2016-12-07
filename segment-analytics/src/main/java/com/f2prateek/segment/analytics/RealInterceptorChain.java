package com.f2prateek.segment.analytics;

import com.f2prateek.segment.model.Message;
import java.util.List;
import java.util.Map;

import static com.f2prateek.segment.analytics.Utils.isNullOrEmpty;

class RealInterceptorChain implements Interceptor.Chain {
  int index;
  final Message message;
  final List<Interceptor> interceptors;
  final List<Integration> integrations;

  RealInterceptorChain(int index, Message message, List<Interceptor> interceptors,
      List<Integration> integrations) {
    this.index = index;
    this.message = message;
    this.interceptors = interceptors;
    this.integrations = integrations;
  }

  @Override public Message message() {
    return message;
  }

  @Override public void proceed(final Message message) {
    // If there's another interceptor in the chain, call that.
    if (index < interceptors.size()) {
      Interceptor.Chain chain =
          new RealInterceptorChain(index + 1, message, interceptors, integrations);
      interceptors.get(index).intercept(chain);
      return;
    }

    // No more interceptors. Send to integrations.
    Map<String, Object> options = message.integrations();

    // Get what the default option is. This is true, unless an explicit value has been provided
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
  }
}