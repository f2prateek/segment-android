package com.f2prateek.segment.model;

import android.support.annotation.NonNull;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.f2prateek.segment.model.Utils.assertNotNull;
import static com.f2prateek.segment.model.Utils.assertNotNullOrEmpty;

public final class Batch {
  private final @NonNull List<Message> batch;
  private final @NonNull Date sentAt;

  @Private Batch(@NonNull List<Message> batch, @NonNull Date sentAt) {
    this.batch = batch;
    this.sentAt = sentAt;
  }

  public List<Message> batch() {
    return batch;
  }

  public Date sentAt() {
    return sentAt;
  }

  @Override public String toString() {
    return "Batch{" + "batch=" + batch + ", " + "sentAt=" + sentAt + "}";
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Batch) {
      Batch that = (Batch) o;
      return (this.batch.equals(that.batch())) && (this.sentAt.equals(that.sentAt()));
    }
    return false;
  }

  @Override public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= this.batch.hashCode();
    h *= 1000003;
    h ^= this.sentAt.hashCode();
    return h;
  }

  /** Fluent API for creating {@link Batch} instances. */
  public final static class Builder {
    private List<Message> batch;
    private Date sentAt;

    public @NonNull Builder batch(@NonNull List<Message> batch) {
      this.batch = Collections.unmodifiableList(assertNotNullOrEmpty(batch, "batch"));
      return this;
    }

    public @NonNull Builder sentAt(@NonNull Date sentAt) {
      this.sentAt = assertNotNull(sentAt, "sentAt");
      return this;
    }

    public Batch build() {
      assertNotNullOrEmpty(batch, "batch");

      Date sentAt = this.sentAt;
      if (sentAt == null) {
        sentAt = new Date();
      }

      return new Batch(batch, sentAt);
    }
  }
}
