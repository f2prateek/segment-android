package com.f2prateek.segment;

import com.squareup.tape2.ObjectQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.mock.Calls;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class) //
@Config(constants = BuildConfig.class, sdk = 23) //
public class TransporterTest {
  @Mock TrackingAPI trackingAPI;
  ObjectQueue<Message> queue;
  Transporter transporter;

  @Before public void setUp() {
    MockitoAnnotations.initMocks(this);
    queue = ObjectQueue.createInMemory();
    transporter = new Transporter(queue, trackingAPI);
  }

  @Test public void trimsBatches() throws Exception {
    for (int i = 0; i < 40; i++) {
      queue.add(new TrackMessage.Builder().userId("userId").event("event").build());
    }

    Call<Void> call = Calls.response(Response.success((Void) null));
    when(trackingAPI.batch(any(Batch.class))).thenReturn(call);

    transporter.flush();

    ArgumentCaptor<Batch> batchArgumentCaptor = ArgumentCaptor.forClass(Batch.class);
    verify(trackingAPI).batch(batchArgumentCaptor.capture());

    assertThat(batchArgumentCaptor.getValue().batch()).hasSize(33);
  }
}
