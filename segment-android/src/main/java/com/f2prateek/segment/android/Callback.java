package com.f2prateek.segment.android;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.f2prateek.segment.model.Message;

public interface Callback {
  enum Event {
    PERSIST, UPLOAD
  }

  // Indicates that the event completed successfully for the given message. 
  void success(@NonNull Event event, @NonNull Message message);

  // Indicates that the event failed for the given message. 
  void failure(@NonNull Event event, @NonNull Message message, @Nullable Throwable error);
}
