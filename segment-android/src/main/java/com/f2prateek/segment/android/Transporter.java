package com.f2prateek.segment.android;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.f2prateek.segment.model.Batch;
import com.f2prateek.segment.model.Message;
import com.squareup.tape2.ObjectQueue;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import retrofit2.Response;

import static com.f2prateek.segment.android.Callback.Event.PERSIST;
import static com.f2prateek.segment.android.Callback.Event.UPLOAD;

class Transporter {
  @Private final ObjectQueue<Message> queue;
  @Private final TrackingAPI trackingAPI;
  private final ExecutorService executor;
  @Nullable private final Callback callback;

  Transporter(ObjectQueue<Message> queue, TrackingAPI trackingAPI, @Nullable Callback callback) {
    this.queue = queue;
    this.trackingAPI = trackingAPI;
    this.callback = callback;
    executor = Executors.newSingleThreadExecutor();
  }

  @NonNull <T extends Message> Future<T> enqueue(@NonNull final T message) {
    return executor.submit(new Callable<T>() {
      @Override public T call() throws Exception {
        try {
          queue.add(message);
          if (callback != null) {
            callback.success(PERSIST, message);
          }
        } catch (IOException e) {
          if (callback != null) {
            callback.error(PERSIST, message, e);
          }
          throw e;
        }

        return message;
      }
    });
  }

  @NonNull Future<List<Message>> flush() {
    return executor.submit(new Callable<List<Message>>() {
      @Override public List<Message> call() throws Exception {
        List<Message> messages = queue.peek(JsonUtils.MAX_BATCH_COUNT);
        final Batch batch = new Batch.Builder().batch(messages).build();
        try {
          Response response = trackingAPI.batch(batch).execute();
          if (response.isSuccessful()) {
            queue.clear();
          }
          if (callback != null) {
            for (Message message : messages) {
              callback.success(UPLOAD, message);
            }
          }
        } catch (IOException e) {
          if (callback != null) {
            for (Message message : messages) {
              callback.error(UPLOAD, message, e);
            }
          }
          throw e;
        }
        return messages;
      }
    });
  }
}