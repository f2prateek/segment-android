package com.f2prateek.segment.android;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import com.f2prateek.segment.model.AliasMessage;
import com.f2prateek.segment.model.Batch;
import com.f2prateek.segment.model.GroupMessage;
import com.f2prateek.segment.model.IdentifyMessage;
import com.f2prateek.segment.model.Message;
import com.f2prateek.segment.model.ScreenMessage;
import com.f2prateek.segment.model.TrackMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.f2prateek.segment.android.Utils.isNullOrEmpty;

final class JsonUtils {
  // Single message is limited to 15kb.
  static final int MAX_MESSAGE_SIZE = 15 << 10;
  // Batch message is limited to 500kb.
  private static final int MAX_BATCH_SIZE = 500 << 10;
  // Try to guarantee delivery by ensuring we don't try to upload more events than we're allowed.
  static final int MAX_BATCH_COUNT = MAX_BATCH_SIZE / MAX_MESSAGE_SIZE;

  private JsonUtils() {
    throw new AssertionError("No instances");
  }

  static void toJson(JsonWriter writer, Batch batch) throws IOException {
    writer.beginObject();

    writer.name("batch").beginArray();
    for (Message message : batch.batch()) {
      toJson(writer, message);
    }
    writer.endArray();

    writer.name("sentAt").value(Iso8601Utils.format(batch.sentAt()));

    writer.endObject();
  }

  @SuppressWarnings("ConstantConditions") static Message fromJson(JsonReader reader)
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
          context = readerToMap(reader);
          break;
        }
        case "integrations": {
          integrations = readerToMap(reader);
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
          traits = readerToMap(reader);
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
          properties = readerToMap(reader);
          break;
        }
        default: {
          reader.skipValue();
        }
      }
    }
    reader.endObject();

    final Message.Builder builder;

    switch (type) {
      case alias:
        builder = new AliasMessage.Builder().previousId(previousId);
        break;
      case group:
        GroupMessage.Builder groupBuilder = new GroupMessage.Builder().groupId(groupId);
        builder = groupBuilder;

        if (!isNullOrEmpty(traits)) {
          groupBuilder.traits(traits);
        }
        break;
      case identify:
        IdentifyMessage.Builder identifyBuilder = new IdentifyMessage.Builder();
        builder = identifyBuilder;

        if (!isNullOrEmpty(traits)) {
          identifyBuilder.traits(traits);
        }
        break;
      case screen:
        ScreenMessage.Builder screenBuilder = new ScreenMessage.Builder().name(name);
        builder = screenBuilder;

        if (!isNullOrEmpty(properties)) {
          screenBuilder.properties(properties);
        }
        break;
      case track:
        TrackMessage.Builder trackBuilder = new TrackMessage.Builder().event(event);
        builder = trackBuilder;

        if (!isNullOrEmpty(properties)) {
          trackBuilder.properties(properties);
        }
        break;
      default:
        throw new AssertionError("unknown message type: " + type);
    }

    if (messageId != null) {
      builder.messageId(messageId);
    }

    if (timestamp != null) {
      builder.timestamp(timestamp);
    }

    if (!isNullOrEmpty(integrations)) {
      builder.context(context);
    }

    if (!isNullOrEmpty(integrations)) {
      builder.integrations(integrations);
    }

    if (userId != null) {
      builder.userId(userId);
    }

    if (anonymousId != null) {
      builder.anonymousId(anonymousId);
    }

    return builder.build();
  }

  static void toJson(JsonWriter writer, Message message) throws IOException {
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
      mapToWriter(context, writer);
    }

    Map<String, Object> integrations = message.integrations();
    if (integrations != null) {
      writer.name("integrations");
      mapToWriter(integrations, writer);
    }

    String userId = message.userId();
    if (userId != null) writer.name("userId").value(userId);

    String anonymousId = message.anonymousId();
    if (anonymousId != null) writer.name("anonymousId").value(anonymousId);

    switch (message.type()) {
      case alias:
        String previousId = ((AliasMessage) message).previousId();
        writer.name("previousId").value(previousId);
        break;
      case group:
        GroupMessage groupMessage = ((GroupMessage) message);

        writer.name("groupId").value(groupMessage.groupId());

        Map<String, Object> groupTraits = groupMessage.traits();
        if (groupTraits != null) {
          writer.name("traits");
          mapToWriter(groupTraits, writer);
        }
        break;
      case identify:
        Map<String, Object> identifyTraits = ((IdentifyMessage) message).traits();
        if (identifyTraits != null) {
          writer.name("traits");
          mapToWriter(identifyTraits, writer);
        }
        break;
      case screen:
        ScreenMessage screenMessage = ((ScreenMessage) message);

        writer.name("name").value(screenMessage.name());

        Map<String, Object> screenProperties = screenMessage.properties();
        if (screenProperties != null) {
          writer.name("properties");
          mapToWriter(screenProperties, writer);
        }
        break;
      case track:
        TrackMessage trackMessage = ((TrackMessage) message);

        writer.name("event").value(trackMessage.event());

        Map<String, Object> trackProperties = trackMessage.properties();
        if (trackProperties != null) {
          writer.name("properties");
          mapToWriter(trackProperties, writer);
        }
        break;
      default:
        throw new AssertionError("unknown message type: " + message.type());
    }

    writer.endObject();
  }

  static final class MessageTooLargeException extends IOException {
    final Message message;
    final long count;

    MessageTooLargeException(Message message, long count) {
      super("Json must be less than " + JsonUtils.MAX_MESSAGE_SIZE);
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

  // Adapted from Cartographer: https://github.com/segmentio/cartographer.

  /** Reads the {@link JsonReader} into a {@link Map}. */
  private static Map<String, Object> readerToMap(JsonReader reader) throws IOException {
    Map<String, Object> map = new LinkedHashMap<>();
    reader.beginObject();
    while (reader.hasNext()) {
      map.put(reader.nextName(), readValue(reader));
    }
    reader.endObject();
    return map;
  }

  /** Reads the {@link JsonReader} into a {@link List}. */
  private static List<Object> readerToList(JsonReader reader) throws IOException {
    // todo: try to infer the type of the List?
    List<Object> list = new ArrayList<>();
    reader.beginArray();
    while (reader.hasNext()) {
      list.add(readValue(reader));
    }
    reader.endArray();
    return list;
  }

  /** Reads the next value in the {@link JsonReader}. */
  private static Object readValue(JsonReader reader) throws IOException {
    JsonToken token = reader.peek();
    switch (token) {
      case BEGIN_OBJECT:
        return readerToMap(reader);
      case BEGIN_ARRAY:
        return readerToList(reader);
      case BOOLEAN:
        return reader.nextBoolean();
      case NULL:
        reader.nextNull(); // consume the null token
        return null;
      case NUMBER:
        return reader.nextDouble();
      case STRING:
        return reader.nextString();
      default:
        throw new IllegalStateException("Invalid token " + token);
    }
  }

  // Encoding

  /** Encode the given {@link Map} into the {@link JsonWriter}. */
  private static void mapToWriter(Map<?, ?> map, JsonWriter writer) throws IOException {
    writer.beginObject();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      writer.name(String.valueOf(entry.getKey()));
      writeValue(entry.getValue(), writer);
    }
    writer.endObject();
  }

  /** Print the json representation of a List to the given writer. */
  private static void listToWriter(List<?> list, JsonWriter writer) throws IOException {
    writer.beginArray();
    for (Object value : list) {
      writeValue(value, writer);
    }
    writer.endArray();
  }

  /**
   * Writes the given {@link Object} to the {@link JsonWriter}.
   *
   * @throws IOException
   */
  private static void writeValue(Object value, JsonWriter writer) throws IOException {
    if (value == null) {
      writer.nullValue();
    } else if (value instanceof Number) {
      writer.value((Number) value);
    } else if (value instanceof Boolean) {
      writer.value((Boolean) value);
    } else if (value instanceof List) {
      listToWriter((List) value, writer);
    } else if (value instanceof Map) {
      mapToWriter((Map) value, writer);
    } else {
      writer.value(String.valueOf(value));
    }
  }
}
