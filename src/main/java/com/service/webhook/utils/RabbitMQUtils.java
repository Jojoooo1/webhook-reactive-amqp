package com.service.webhook.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.Message;

@Slf4j
public class RabbitMQUtils {

  public static final String RETRY_HEADER = "x-retry-count";
  public static final String DELAY_HEADER = "x-delay";
  public static final String URL_HEADER = "url";

  public static String getUrl(final Message<LinkedHashMap<String, Object>> message) {
    return (String) message.getHeaders().get(URL_HEADER);
  }

  public static HashMap<String, String> getHeaders(
      final Message<LinkedHashMap<String, Object>> message) {

    final HashMap<String, String> headers = new HashMap<>();

    for (final Map.Entry<String, Object> entry : message.getHeaders().entrySet()) {
      if (entry.getValue() instanceof String) {
        headers.put(entry.getKey(), entry.getValue().toString());
      }
    }

    return headers;
  }

  public static Integer getRetryCount(final Map<String, String> headers) {
    return StringUtils.isBlank(headers.get(RETRY_HEADER))
        ? 0
        : Integer.parseInt(headers.get(RETRY_HEADER));
  }

  public static Integer getRandom(final int min, final int max) {
    final Random random = new Random();
    return random.ints(min, max).findFirst().getAsInt();
  }
}
