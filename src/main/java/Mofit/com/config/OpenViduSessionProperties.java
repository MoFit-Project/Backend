package Mofit.com.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import io.openvidu.java.client.*;

import java.lang.reflect.Type;
import java.util.Map;


// 일반적으로 wrapper 클래스는 빈으로 등록하지 않는다.
// 대신, 해당 클래스의 인스턴스를 생성하여 사용하는 방식으로 사용된다
public class OpenViduSessionProperties {

    private SessionProperties sessionProperties;

    public OpenViduSessionProperties() {
        this.sessionProperties = new SessionProperties.Builder().build();
    }

    public SessionProperties.Builder getJson(Map<String, ?> params) throws IllegalArgumentException {

        SessionProperties.Builder builder = new SessionProperties.Builder();
        String customSessionId = null;

        if (params != null) {

            // Obtain primitive values from the params map
            String mediaModeString;
            String recordingModeString;
            String forcedVideoCodecStr;
            Boolean allowTranscoding;
            try {
                mediaModeString = (String) params.get("mediaMode");
                recordingModeString = (String) params.get("recordingMode");
                customSessionId = (String) params.get("customSessionId");
                forcedVideoCodecStr = (String) params.get("forcedVideoCodec");
                allowTranscoding = (Boolean) params.get("allowTranscoding");
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Type error in some parameter: " + e.getMessage());
            }

            // Parse obtained values into actual types
            VideoCodec forcedVideoCodec = null;
            try {
                forcedVideoCodec = VideoCodec.valueOf(forcedVideoCodecStr);
            } catch (NullPointerException e) {
                // Not an error: "forcedVideoCodec" was not provided in params.
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid value for parameter 'forcedVideoCodec': " + e.getMessage());
            }

            try {
                // Safe parameter retrieval. Let default values if not defined
                if (recordingModeString != null) {
                    RecordingMode recordingMode = RecordingMode.valueOf(recordingModeString);
                    builder = builder.recordingMode(recordingMode);
                }
                if (mediaModeString != null) {
                    MediaMode mediaMode = MediaMode.valueOf(mediaModeString);
                    builder = builder.mediaMode(mediaMode);
                }
                if (customSessionId != null && !customSessionId.isEmpty()) {
                    if (!isValidCustomSessionId(customSessionId)) {
                        throw new IllegalArgumentException(
                                "Parameter 'customSessionId' is wrong. Must be an alphanumeric string [a-zA-Z0-9_-]");
                    }
                    builder = builder.customSessionId(customSessionId);
                }
                if (forcedVideoCodec != null) {
                    builder = builder.forcedVideoCodec(forcedVideoCodec);
                    builder = builder.forcedVideoCodecResolved(forcedVideoCodec);
                }
                if (allowTranscoding != null) {
                    builder = builder.allowTranscoding(allowTranscoding);
                }

                JsonObject defaultRecordingPropertiesJson = null;
                if (params.get("defaultRecordingProperties") != null) {
                    try {
                        defaultRecordingPropertiesJson = new Gson()
                                .toJsonTree(params.get("defaultRecordingProperties"), Map.class).getAsJsonObject();
                    } catch (Exception e) {
                        throw new IllegalArgumentException(
                                "Error in parameter 'defaultRecordingProperties'. It is not a valid JSON object");
                    }
                }
                if (defaultRecordingPropertiesJson != null) {
                    try {
                        String jsonString = defaultRecordingPropertiesJson.toString();
                        RecordingProperties.Builder recBuilder = RecordingProperties
                                .fromJson(new Gson().fromJson(jsonString, Map.class), null);
                        RecordingProperties defaultRecordingProperties = recBuilder.build();
                        builder = builder.defaultRecordingProperties(defaultRecordingProperties);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(
                                "Parameter 'defaultRecordingProperties' is not valid: " + e.getMessage());
                    }
                }

                String mediaNode = getMediaNodeProperty(params);
                if (mediaNode != null) {
                    builder = builder.mediaNode(mediaNode);
                }

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Some parameter is not valid. " + e.getMessage());
            }
        }
        return builder;
    }

    /**
     * @hidden
     */
    public static String getMediaNodeProperty(Map<?, ?> params) throws IllegalArgumentException {
        if (params.containsKey("mediaNode") && params.get("mediaNode") != null) {
            JsonObject mediaNodeJson;
            try {
                mediaNodeJson = JsonParser.parseString(params.get("mediaNode").toString()).getAsJsonObject();
            } catch (Exception e) {
                try {
                    Gson gson = new Gson();
                    Type gsonType = new TypeToken<Map>() {
                    }.getType();
                    String gsonString = gson.toJson(params.get("mediaNode"), gsonType);
                    mediaNodeJson = JsonParser.parseString(gsonString).getAsJsonObject();
                } catch (Exception e2) {
                    throw new IllegalArgumentException("Error in parameter 'mediaNode'. It is not a valid JSON object");

                }
            }
            if (!mediaNodeJson.has("id")) {
                throw new IllegalArgumentException("Error in parameter 'mediaNode'. Property 'id' not found");
            }
            String mediaNode;
            try {
                JsonPrimitive primitive = mediaNodeJson.get("id").getAsJsonPrimitive();
                if (!primitive.isString()) {
                    throw new IllegalArgumentException("Type error in parameter 'mediaNode.id': not a String");
                } else {
                    mediaNode = primitive.getAsString();
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Type error in parameter 'mediaNode.id': " + e.getMessage());
            }
            return mediaNode;
        }
        return null;
    }
    public SessionProperties getSessionProperties() {
        return sessionProperties;
    }


    public boolean isValidCustomSessionId(String customSessionId) {
        return customSessionId.matches("[a-zA-Z0-9가-힣!@~_-]+");
    }
}
