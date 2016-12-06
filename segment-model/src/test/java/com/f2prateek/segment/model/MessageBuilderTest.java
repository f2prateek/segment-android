package com.f2prateek.segment.model;

import com.google.common.collect.ImmutableMap;
import com.squareup.burst.BurstJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings("ConstantConditions") //
@RunWith(BurstJUnit4.class) //
public class MessageBuilderTest {

  @SuppressWarnings("unchecked") //
  enum MessageBuilder {
    ALIAS {
      @Override public AliasMessage.Builder get() {
        return new AliasMessage.Builder().previousId("previousId");
      }
    }, GROUP {
      @Override public GroupMessage.Builder get() {
        return new GroupMessage.Builder().groupId("groupId");
      }
    },
    IDENTIFY {
      @Override public IdentifyMessage.Builder get() {
        return new IdentifyMessage.Builder();
      }
    }, SCREEN {
      @Override public ScreenMessage.Builder get() {
        return new ScreenMessage.Builder().name("name");
      }
    }, TRACK {
      @Override public TrackMessage.Builder get() {
        return new TrackMessage.Builder().event("event");
      }
    };

    public abstract Message.Builder<?, ?> get();
  }

  @Test public void nullMessageIdThrowsException(MessageBuilder builder) {
    try {
      builder.get().messageId(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("messageId cannot be null or empty");
    }
  }

  @Test public void defaultMessageIdIsGenerated(MessageBuilder builder) {
    Message message = builder.get().userId("userId").build();
    assertThat(message.messageId()).isNotNull();
  }

  @Test public void messageIdCanBeProvided(MessageBuilder builder) {
    Message message = builder.get().userId("userId").messageId("foo").build();
    assertThat(message.messageId()).isEqualTo("foo");
  }

  @Test public void defaultAnonymousIdIsNotGenerated(MessageBuilder builder) {
    Message message = builder.get().userId("foo").build();
    assertThat(message.anonymousId()).isNull();
  }

  @Test public void anonymousIdCanBeProvided(MessageBuilder builder) {
    // Must also provide a userId because identify requires `userId` or `traits`.
    Message message = builder.get().anonymousId("foo").userId("userId").build();
    assertThat(message.anonymousId()).isEqualTo("foo");
  }

  @Test public void missingUserIdAndAnonymousIdThrowsException(MessageBuilder builder) {
    try {
      builder.get().build();
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("either userId or anonymousId is required");
    }
  }

  @Test public void nullTimestampThrowsError(MessageBuilder builder) {
    try {
      builder.get().timestamp(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("timestamp == null");
    }
  }

  @Test public void providingUserIdBuildsSuccessfully(MessageBuilder builder) {
    Message message = builder.get().userId("foo").build();
    assertThat(message.userId()).isEqualTo("foo");
  }

  @Test public void aliasBuilder() {
    try {
      new AliasMessage.Builder().previousId(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("previousId cannot be null or empty");
    }
  }

  @Test public void groupBuilder() {
    try {
      new GroupMessage.Builder().groupId(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("groupId cannot be null or empty");
    }

    try {
      new GroupMessage.Builder().groupId("groupId").traits(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("traits == null");
    }
  }

  @Test public void identifyBuilder() {
    try {
      new IdentifyMessage.Builder().traits(null);
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("traits == null");
    }

    try {
      new IdentifyMessage.Builder().build();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("either userId or anonymousId is required");
    }
  }

  @Test public void screenBuilder() {
    try {
      new ScreenMessage.Builder().name(null);
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("name cannot be null or empty");
    }

    try {
      new ScreenMessage.Builder().properties(null).build();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("properties == null");
    }
  }

  @Test public void trackBuilder() {
    try {
      new TrackMessage.Builder().event(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("event cannot be null or empty");
    }

    try {
      new TrackMessage.Builder().properties(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("properties == null");
    }
  }

  @Test public void defaultIntegrationsIsGenerated(MessageBuilder builder) {
    Message message = builder.get().userId("foo").build();

    assertThat(message.integrations()).isEqualTo(ImmutableMap.of());
  }

  @Test public void integrations(MessageBuilder builder) {
    Message message = builder.get()
        .userId("userId")
        .integration("foo", false)
        .integration("bar", ImmutableMap.of("qaz", "qux"))
        .build();

    assertThat(message.integrations()).hasSize(2);
    assertThat(message.integrations()).containsEntry("foo", false);
    assertThat(message.integrations()).containsEntry("bar", ImmutableMap.of("qaz", "qux"));
  }
}
