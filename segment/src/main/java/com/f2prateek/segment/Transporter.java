package com.f2prateek.segment;

import android.support.annotation.NonNull;
import com.squareup.tape2.ObjectQueue;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import retrofit2.Response;

class Transporter {
  // Batch message is limited to 500kb.
  private static final int MAX_BATCH_SIZE = 500000;
  // Guarantee delivery by ensuring we don't try to upload more events than we're allowed.
  private static final int MAX_BATCH_COUNT =
      MAX_BATCH_SIZE / MoshiMessageConverter.MAX_MESSAGE_SIZE;
  @Private final ObjectQueue<Message> queue;
  @Private final TrackingAPI trackingAPI;
  private final ExecutorService executor;

  Transporter(ObjectQueue<Message> queue, TrackingAPI trackingAPI) {
    this.queue = queue;
    this.trackingAPI = trackingAPI;
    executor = Executors.newSingleThreadExecutor();
  }

  @NonNull <T extends Message> Future<T> enqueue(@NonNull final T message) {
    return executor.submit(new Callable<T>() {
      @Override public T call() throws Exception {
        queue.add(message);
        return message;
      }
    });
  }

  @NonNull Future<List<Message>> flush() {
    return executor.submit(new Callable<List<Message>>() {
      @Override public List<Message> call() throws Exception {
        List<Message> messages = queue.peek(MAX_BATCH_COUNT);
        final Batch batch = Batch.create(messages);
        Response response = trackingAPI.batch(batch).execute();
        if (response.isSuccessful()) {
          queue.clear();
        }
        return messages;
      }
    });
  }
}