/*
 *
 * Copyright © 2024 Applause App Quality, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.applause.auto.framework.json;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

/** Helper for mapping objects to a String */
@SuppressWarnings("unchecked")
public final class GsonHelper {
  // Gson is thread safe.  We keep our own Gson instance in case we need to configure
  // it in some special way (then there's just one place to change the code)
  private static final Gson gson = new Gson();

  private GsonHelper() {
    // Only static methods
  }

  /**
   * Serializes a Map to a String
   *
   * @param map The Map to serialize
   * @return The serialized map
   */
  public static String map2JsonString(@NonNull final Map<String, Object> map) {
    return gson.toJson(map);
  }

  /**
   * Get the common Gson Instance used in the Provider Capabilities feature
   *
   * @return A common Gson instance
   */
  public static Gson ourGson() {
    return gson;
  }

  /**
   * Convert a JsonObject to a Map. Gson has a nasty feature where integer/long values are
   * represented as Double. So 3000 gets exported as 3000.0.
   *
   * @param obj The object to convert
   * @return A map, unless the object was null or empty JSON
   * @throws BadJsonFormatException if the JsonObject cannot be converted to a Map
   */
  public static Map<String, Object> jsonObject2Map(@NonNull final JsonObject obj)
      throws BadJsonFormatException {
    Type type = new TypeToken<Map<String, Object>>() {}.getType();
    try {
      var result =
          Optional.ofNullable((Map<String, Object>) gson.fromJson(obj, type))
              .orElseThrow(
                  () ->
                      new BadJsonFormatException(" JSON object " + obj + " was empty (not a map)"));
      // We need to find all values that were originally int/long that Gson has converted to Double
      fixGsonDoublesInMap(result, obj);
      return result;
    } catch (JsonSyntaxException jse) {
      throw new BadJsonFormatException("conversion from JSON Object to map failed", jse);
    }
  }

  static void fixGsonDoublesInMap(final Map<String, Object> map, final JsonObject original) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      final String key = entry.getKey();
      final Object val = entry.getValue();
      if (val instanceof Double) {
        // Was the original a double?
        map.put(key, fixGsonDouble(val, original.get(key)));
        continue;
      }
      if (val instanceof Map) {
        // We have a map that we need to follow
        fixGsonDoublesInMap((Map<String, Object>) val, original.getAsJsonObject(key));
        continue;
      }
      if (val instanceof List) {
        List<Object> fixedArray =
            fixGsonDoublesInArray((List<Object>) val, original.getAsJsonArray(key));
        map.put(key, fixedArray);
      }
    }
  }

  static List<Object> fixGsonDoublesInArray(
      final List<Object> valArray, final JsonArray asJsonArray) {
    List<Object> result = new LinkedList<>();
    int idx = -1;
    for (Object jsonElem : valArray) {
      idx++;
      Object fixedObj = jsonElem;
      if (jsonElem instanceof Double) {
        fixedObj = fixGsonDouble(jsonElem, asJsonArray.get(idx));
      } else if (jsonElem instanceof Map) {
        fixGsonDoublesInMap((Map<String, Object>) jsonElem, asJsonArray.get(idx).getAsJsonObject());
      } else if (jsonElem instanceof List) {
        fixedObj =
            fixGsonDoublesInArray((List<Object>) jsonElem, asJsonArray.get(idx).getAsJsonArray());
      }
      result.add(fixedObj);
    }
    return result;
  }

  static Object fixGsonDouble(final Object valueFromMap, final JsonElement originalElem) {
    Object result = valueFromMap;
    if (valueFromMap instanceof Double) {
      // Was the original a double?
      final String orgStrVal = originalElem.toString();
      if (!orgStrVal.contains(".")) {
        // The original value doesn't have a decimal point, so we really want a long here instead of
        // a Double
        result = ((Double) valueFromMap).longValue();
      }
    }
    return result;
  }

  /**
   * Convert a string into a JsonObject
   *
   * @param jsonStr The string to convert
   * @return a JsonObject
   * @throws BadJsonFormatException when the element cannot be converted to Json
   */
  public static JsonObject str2JsonObject(@NonNull final String jsonStr)
      throws BadJsonFormatException {
    JsonElement elem =
        Preconditions.checkNotNull(
            str2JsonElement(jsonStr), "str2JsonElement converted " + jsonStr + " to null");
    try {
      return elem.getAsJsonObject();
    } catch (IllegalStateException ise) {
      throw new BadJsonFormatException(
          String.format(
              "Expected a single JSON object: '%s'.  Original JSON '%s'",
              ise.getMessage(), jsonStr),
          ise);
    }
  }

  /**
   * Convert a string into a JsonElement
   *
   * @param jsonStr The string to convert
   * @return a JsonElement
   * @throws BadJsonFormatException when element couldn't be converted to JSON
   */
  public static JsonElement str2JsonElement(@NonNull final String jsonStr)
      throws BadJsonFormatException {
    try {
      return ourGson().fromJson(jsonStr, JsonElement.class);
    } catch (JsonSyntaxException jse) {
      throw new BadJsonFormatException(
          String.format("Bad JSON: '%s'.  Original JSON '%s'", jse.getMessage(), jsonStr), jse);
    }
  }

  /**
   * Utility method to parse Generic JSON from an OkHttpRsp and account for the usual error
   * conditions
   *
   * @param okHttpRsp The response from OK HTTP
   * @return null of there was an error. A generic JsonObject on successful data extraction
   */
  public static JsonObject httpRspToJsonObject(@NonNull final Response okHttpRsp) {
    ResponseBody body = okHttpRsp.body();
    if (body == null) {
      throw new RuntimeException("Unable to parse response - null body");
    }
    // IF the context type is not passed back correctly, then default it to application json
    MediaType contentAndMetaData =
        Optional.ofNullable(body.contentType()).orElse(MediaType.get("application/json"));
    if (!"json".equalsIgnoreCase(contentAndMetaData.subtype())) {
      throw new RuntimeException(
          String.format(
              "Non JSON content: type=%s, subtype=%s",
              contentAndMetaData.type(), contentAndMetaData.subtype()));
    }
    final String rawBody;
    try {
      rawBody = body.string();
      // Check the subtype parsed out by
    } catch (IOException e) {
      // This is really unexpected.  Throw an exception so we get a stack trace
      throw new RuntimeException("Unable to extract body from http response", e);
    }
    try {
      return str2JsonObject(rawBody);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
