package com.service.webhook.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.LinkedHashMap;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class JsonUtils {

  public static String toJSON(final LinkedHashMap<String, Object> payload) {
    final Gson gson = new GsonBuilder().serializeNulls().create();
    return gson.toJson(payload, LinkedHashMap.class);
  }
}
