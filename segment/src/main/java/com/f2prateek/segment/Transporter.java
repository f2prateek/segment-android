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
        List<Message> messages = queue.asList();
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