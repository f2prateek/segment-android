package com.f2prateek.segment.android;

import com.f2prateek.segment.model.Batch;
import com.f2prateek.segment.model.Message;
import com.f2prateek.segment.model.TrackMessage;
import com.squareup.tape2.ObjectQueue;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.mock.Calls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class) //
@Config(constants = BuildConfig.class, sdk = 23) //
public class TransporterTest {
  @Mock TrackingAPI trackingAPI;
  @Mock Callback callback;
  ObjectQueue<Message> queue;
  Transporter transporter;

  @Before public void setUp() {
    MockitoAnnotations.initMocks(this);
    queue = ObjectQueue.createInMemory();
    transporter = new Transporter(queue, trackingAPI, callback);
  }

  @Test public void invokesCallback() throws ExecutionException, InterruptedException {
    Message message = new TrackMessage.Builder().userId("userId").event("event").build();

    transporter.enqueue(message).get();
    verify(callback).success(Callback.Event.PERSIST, message);

    Call<Void> call = Calls.response(Response.success((Void) null));
    when(trackingAPI.batch(any(Batch.class))).thenReturn(call);
    transporter.flush().get();
    verify(callback).success(Callback.Event.UPLOAD, message);
  }

  @Test public void invokesPersistErrorCallback() throws Exception {
    //noinspection unchecked
    queue = mock(ObjectQueue.class);
    transporter = new Transporter(queue, trackingAPI, callback);

    Message message = new TrackMessage.Builder().userId("userId").event("event").build();
    IOException testException = new IOException("test");

    Mockito.doThrow(testException).when(queue).add(message);

    try {
      transporter.enqueue(message).get();
    } catch (ExecutionException e) {
      assertThat(e.getCause()).isEqualTo(testException);
    }
    verify(callback).error(Callback.Event.PERSIST, message, testException);
  }

  @Test public void invokesUploadErrorCallback() throws Exception {
    transporter = new Transporter(queue, trackingAPI, callback);

    Message message = new TrackMessage.Builder().userId("userId").event("event").build();
    IOException testException = new IOException("test");

    Call<Void> call = Calls.failure(testException);
    when(trackingAPI.batch(any(Batch.class))).thenReturn(call);

    transporter.enqueue(message).get();

    try {
      transporter.flush().get();
    } catch (ExecutionException e) {
      assertThat(e.getCause()).isEqualTo(testException);
    }
    verify(callback).error(Callback.Event.UPLOAD, message, testException);
  }

  @Test public void ignoresNullCallback() throws ExecutionException, InterruptedException {
    transporter = new Transporter(queue, trackingAPI, null);

    Message message = new TrackMessage.Builder().userId("userId").event("event").build();

    transporter.enqueue(message).get();

    Call<Void> call = Calls.response(Response.success((Void) null));
    when(trackingAPI.batch(any(Batch.class))).thenReturn(call);
    transporter.flush().get();
  }

  @Test public void trimsBatches() throws Exception {
    for (int i = 0; i < 40; i++) {
      queue.add(new TrackMessage.Builder().userId("userId").event("event").build());
    }

    Call<Void> call = Calls.response(Response.success((Void) null));
    when(trackingAPI.batch(any(Batch.class))).thenReturn(call);

    transporter.flush().get();

    ArgumentCaptor<Batch> batchArgumentCaptor = ArgumentCaptor.forClass(Batch.class);
    verify(trackingAPI).batch(batchArgumentCaptor.capture());

    assertThat(batchArgumentCaptor.getValue().batch()).hasSize(33);
  }
}
