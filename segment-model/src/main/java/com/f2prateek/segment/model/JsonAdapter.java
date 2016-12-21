package com.f2prateek.segment.model;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

public final class JsonAdapter {
  // Single message is limited to 15kb.
  public static final int MAX_MESSAGE_SIZE = 15 << 10;
  // Batch message is limited to 500kb.
  private static final int MAX_BATCH_SIZE = 500 << 10;
  // Try to guarantee delivery by ensuring we don't try to upload more events than we're allowed.
  public static final int MAX_BATCH_COUNT = MAX_BATCH_SIZE / MAX_MESSAGE_SIZE;

  private JsonAdapter() {
    throw new AssertionError("No instances");
  }

  public static void toJson(JsonWriter writer, Batch batch) throws IOException {
    writer.beginObject();

    writer.name("batch").beginArray();
    for (Message message : batch.batch()) {
      toJson(writer, message);
    }
    writer.endArray();

    writer.name("sentAt").value(Iso8601Utils.format(batch.sentAt()));

    writer.endObject();
  }

  @SuppressWarnings("ConstantConditions") public static Message fromJson(JsonReader reader)
      throws IOException {
    reader.beginObject();

    // Common fields.
    Message.Type type = null;
    String messageId = null;
    Date timestamp = null;
    Map<String, Object> context = null;
    Map<String, Object> integrations = null;
    String userId = null;
    String anonymousId = null;

    // Alias fields.
    String previousId = null;

    // Group fields.
    String groupId = null;

    // Identify fields.
    // None.

    // Group and Identify fields
    Map<String, Object> traits = null;

    // Screen fields
    String name = null;

    // Track fields
    String event = null;

    // Screen and Track fields
    Map<String, Object> properties = null;

    while (reader.hasNext()) {
      String fieldName = reader.nextName();
      if (reader.peek() == JsonToken.NULL) {
        reader.skipValue();
        continue;
      }
      switch (fieldName) {
        case "type": {
          type = Enum.valueOf(Message.Type.class, reader.nextString());
          break;
        }
        case "messageId": {
          messageId = reader.nextString();
          break;
        }
        case "timestamp": {
          timestamp = Iso8601Utils.parse(reader.nextString());
          break;
        }
        case "context": {
          context = Cartographer.readerToMap(reader);
          break;
        }
        case "integrations": {
          integrations = Cartographer.readerToMap(reader);
          break;
        }
        case "userId": {
          userId = reader.nextString();
          break;
        }
        case "anonymousId": {
          anonymousId = reader.nextString();
          break;
        }
        case "previousId": {
          previousId = reader.nextString();
          break;
        }
        case "groupId": {
          groupId = reader.nextString();
          break;
        }
        case "traits": {
          traits = Cartographer.readerToMap(reader);
          break;
        }
        case "name": {
          name = reader.nextString();
          break;
        }
        case "event": {
          event = reader.nextString();
          break;
        }
        case "properties": {
          properties = Cartographer.readerToMap(reader);
          break;
        }
        default: {
          reader.skipValue();
        }
      }
    }
    reader.endObject();

    switch (type) {
      case alias:
        return new AliasMessage(type, messageId, timestamp, context, integrations, userId,
            anonymousId, previousId);
      case group:
        return new GroupMessage(type, messageId, timestamp, context, integrations, userId,
            anonymousId, groupId, traits);
      case identify:
        return new IdentifyMessage(type, messageId, timestamp, context, integrations, userId,
            anonymousId, traits);
      case screen:
        return new ScreenMessage(type, messageId, timestamp, context, integrations, userId,
            anonymousId, name, properties);
      case track:
        return new TrackMessage(type, messageId, timestamp, context, integrations, userId,
            anonymousId, event, properties);
      default:
        throw new AssertionError("unknown message type: " + type);
    }
  }

  public static void toJson(JsonWriter writer, Message message) throws IOException {
    writer.beginObject();

    // Common fields.
    Message.Type type = message.type();
    writer.name("type").value(type.toString());

    String messageId = message.messageId();
    if (messageId != null) writer.name("messageId").value(messageId);

    Date timestamp = message.timestamp();
    if (timestamp != null) writer.name("timestamp").value(Iso8601Utils.format(timestamp));

    Map<String, Object> context = message.context();
    if (context != null) {
      writer.name("context");
      Cartographer.mapToWriter(context, writer);
    }

    Map<String, Object> integrations = message.integrations();
    if (integrations != null) {
      writer.name("integrations");
      Cartographer.mapToWriter(integrations, writer);
    }

    String userId = message.userId();
    if (userId != null) writer.name("userId").value(userId);

    String anonymousId = message.anonymousId();
    if (anonymousId != null) writer.name("anonymousId").value(anonymousId);

    switch (message.type()) {
      case alias:
        String previousId = ((AliasMessage) message).previousId();
        if (previousId != null) writer.name("previousId").value(previousId);
        break;
      case group:
        GroupMessage groupMessage = ((GroupMessage) message);

        writer.name("groupId").value(groupMessage.groupId());

        Map<String, Object> groupTraits = groupMessage.traits();
        if (groupTraits != null) {
          writer.name("traits");
          Cartographer.mapToWriter(groupTraits, writer);
        }
        break;
      case identify:
        Map<String, Object> identifyTraits = ((IdentifyMessage) message).traits();
        if (identifyTraits != null) {
          writer.name("traits");
          Cartographer.mapToWriter(identifyTraits, writer);
        }
        break;
      case screen:
        ScreenMessage screenMessage = ((ScreenMessage) message);

        writer.name("name").value(screenMessage.name());

        Map<String, Object> screenProperties = screenMessage.properties();
        if (screenProperties != null) {
          writer.name("properties");
          Cartographer.mapToWriter(screenProperties, writer);
        }
        break;
      case track:
        TrackMessage trackMessage = ((TrackMessage) message);

        writer.name("event").value(trackMessage.event());

        Map<String, Object> trackProperties = trackMessage.properties();
        if (trackProperties != null) {
          writer.name("properties");
          Cartographer.mapToWriter(trackProperties, writer);
        }
        break;
      default:
        throw new AssertionError("unknown message type: " + message.type());
    }

    writer.endObject();
  }

  public static final class MessageTooLargeException extends IOException {
    final Message message;
    final long count;

    public MessageTooLargeException(Message message, long count) {
      super("Json must be less than " + JsonAdapter.MAX_MESSAGE_SIZE);
      this.message = message;
      this.count = count;
    }

    @Override public String toString() {
      return "MessageTooLargeException{" +
          "message=" + message +
          ", count=" + count +
          '}';
    }
  }
}
