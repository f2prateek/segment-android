package com.f2prateek.segment;

import com.ryanharter.auto.value.moshi.MoshiAdapterFactory;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@MoshiAdapterFactory //
abstract class SegmentMoshiAdapterFactory implements JsonAdapter.Factory {
  static JsonAdapter.Factory create() {
    final JsonAdapter.Factory generatedFactory = new AutoValueMoshi_SegmentMoshiAdapterFactory();
    final JsonAdapter.Factory messageJsonAdapterFactory = new JsonAdapter.Factory() {
      @Override
      public JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Moshi moshi) {
        if (type != Message.class) return null;
        return new MessageJsonAdapter(moshi);
      }
    };

    return new JsonAdapter.Factory() {
      @Override
      public JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Moshi moshi) {
        JsonAdapter<?> adapter = generatedFactory.create(type, annotations, moshi);
        if (adapter != null) {
          return adapter;
        }

        return messageJsonAdapterFactory.create(type, annotations, moshi);
      }
    };
  }

  /**
   * {@link Message} is abstract and cannot be deserialized (or even serialized) by {@link Moshi}.
   * So we delegate the serializers and write our own deserializer.
   */
  static class MessageJsonAdapter extends JsonAdapter<Message> {
    private final JsonAdapter<IdentifyMessage> identifyMessageJsonAdapter;
    private final JsonAdapter<GroupMessage> groupMessageJsonAdapter;
    private final JsonAdapter<AliasMessage> aliasMessageJsonAdapter;
    private final JsonAdapter<TrackMessage> trackMessageJsonAdapter;
    private final JsonAdapter<ScreenMessage> screenMessageJsonAdapter;

    private final JsonAdapter<Message.Type> typeAdapter;
    private final JsonAdapter<String> messageIdAdapter;
    private final JsonAdapter<Date> timestampAdapter;
    private final JsonAdapter<Map<String, Object>> contextAdapter;
    private final JsonAdapter<Map<String, Object>> integrationsAdapter;
    private final JsonAdapter<String> userIdAdapter;
    private final JsonAdapter<String> anonymousIdAdapter;

    private final JsonAdapter<String> previousIdAdapter;
    private final JsonAdapter<String> groupIdAdapter;
    private final JsonAdapter<Map<String, Object>> traitsAdapter;
    private final JsonAdapter<String> nameAdapter;
    private final JsonAdapter<String> eventAdapter;
    private final JsonAdapter<Map<String, Object>> propertiesAdapter;

    MessageJsonAdapter(Moshi moshi) {
      identifyMessageJsonAdapter = moshi.adapter(IdentifyMessage.class);
      groupMessageJsonAdapter = moshi.adapter(GroupMessage.class);
      aliasMessageJsonAdapter = moshi.adapter(AliasMessage.class);
      trackMessageJsonAdapter = moshi.adapter(TrackMessage.class);
      screenMessageJsonAdapter = moshi.adapter(ScreenMessage.class);

      typeAdapter = moshi.adapter(Message.Type.class);
      messageIdAdapter = moshi.adapter(String.class);
      timestampAdapter = moshi.adapter(Date.class);
      contextAdapter =
          moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
      integrationsAdapter =
          moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
      userIdAdapter = moshi.adapter(String.class);
      anonymousIdAdapter = moshi.adapter(String.class);

      previousIdAdapter = moshi.adapter(String.class);
      groupIdAdapter = moshi.adapter(String.class);
      traitsAdapter =
          moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
      nameAdapter = moshi.adapter(String.class);
      eventAdapter = moshi.adapter(String.class);
      propertiesAdapter =
          moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
    }

    @Override public Message fromJson(JsonReader reader) throws IOException {
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
        if (reader.peek() == JsonReader.Token.NULL) {
          reader.skipValue();
          continue;
        }
        switch (fieldName) {
          case "type": {
            type = typeAdapter.fromJson(reader);
            break;
          }
          case "messageId": {
            messageId = messageIdAdapter.fromJson(reader);
            break;
          }
          case "timestamp": {
            timestamp = timestampAdapter.fromJson(reader);
            break;
          }
          case "context": {
            context = contextAdapter.fromJson(reader);
            break;
          }
          case "integrations": {
            integrations = integrationsAdapter.fromJson(reader);
            break;
          }
          case "userId": {
            userId = userIdAdapter.fromJson(reader);
            break;
          }
          case "anonymousId": {
            anonymousId = anonymousIdAdapter.fromJson(reader);
            break;
          }
          case "previousId": {
            previousId = previousIdAdapter.fromJson(reader);
            break;
          }
          case "groupId": {
            groupId = groupIdAdapter.fromJson(reader);
            break;
          }
          case "traits": {
            traits = traitsAdapter.fromJson(reader);
            break;
          }
          case "name": {
            name = nameAdapter.fromJson(reader);
            break;
          }
          case "event": {
            event = eventAdapter.fromJson(reader);
            break;
          }
          case "properties": {
            properties = propertiesAdapter.fromJson(reader);
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
          return new AutoValue_AliasMessage(type, messageId, timestamp, context, integrations,
              userId, anonymousId, previousId);
        case group:
          return new AutoValue_GroupMessage(type, messageId, timestamp, context, integrations,
              userId, anonymousId, groupId, traits);
        case identify:
          return new AutoValue_IdentifyMessage(type, messageId, timestamp, context, integrations,
              userId, anonymousId, traits);
        case screen:
          return new AutoValue_ScreenMessage(type, messageId, timestamp, context, integrations,
              userId, anonymousId, name, properties);
        case track:
          return new AutoValue_TrackMessage(type, messageId, timestamp, context, integrations,
              userId, anonymousId, event, properties);
        default:
          throw new AssertionError("unknown message type: " + type);
      }
    }

    @Override public void toJson(JsonWriter writer, Message message) throws IOException {
      switch (message.type()) {
        case alias:
          aliasMessageJsonAdapter.toJson(writer, (AliasMessage) message);
          break;
        case group:
          groupMessageJsonAdapter.toJson(writer, (GroupMessage) message);
          break;
        case identify:
          identifyMessageJsonAdapter.toJson(writer, (IdentifyMessage) message);
          break;
        case screen:
          screenMessageJsonAdapter.toJson(writer, (ScreenMessage) message);
          break;
        case track:
          trackMessageJsonAdapter.toJson(writer, (TrackMessage) message);
          break;
        default:
          throw new AssertionError("unknown message type: " + message.type());
      }
    }
  }
}
