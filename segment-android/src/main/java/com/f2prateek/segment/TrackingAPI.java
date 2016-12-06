package com.f2prateek.segment;

import com.f2prateek.segment.model.Batch;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/** REST interface for the Segment Tracking API. */
interface TrackingAPI {
  @POST("/v1/batch") Call<Void> batch(@Body Batch batch);
}