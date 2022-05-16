package com.service.webhook.services;

import com.service.webhook.utils.RabbitMQUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookTemplateService {

  @Value("${service.webhooks.template.exchange}")
  final String exchange;

  @Value("${service.webhooks.template.routingkey}")
  final String routingKey;

  @Value("${service.webhooks.max-retry}")
  final Integer maxRetry;

  @Value("${service.webhooks.min-retry-delay}")
  final Integer minRetryDelay;

  @Value("${service.webhooks.max-retry-delay}")
  final Integer maxRetryDelay;

  private final AmqpTemplate amqpTemplate;

  public void send(
      final String url,
      final Map<String, String> headers,
      final LinkedHashMap<String, Object> payload) {
    try {

      final Integer retryCount = RabbitMQUtils.getRetryCount(headers);

      if (retryCount > (this.maxRetry)) {
        log.info("QUEUE[RETRY_EXHAUSTED] message is being acknowledge");
        return;
      }

      final Integer newRetryCount = retryCount + 1;
      final Integer newDelay =
          RabbitMQUtils.getRandom(this.minRetryDelay, this.maxRetryDelay) * newRetryCount;

      headers.put(RabbitMQUtils.RETRY_HEADER, Integer.toString(newRetryCount));
      headers.put(RabbitMQUtils.DELAY_HEADER, newDelay.toString());

      log.info(
          "QUEUE[webhook] requeuing with retryCount[{}] and delay[{}ms]", newRetryCount, newDelay);
      log.debug("QUEUE[webhook] url[{}] headers[{}] payload[{}]", url, headers, payload);

      this.amqpTemplate.convertAndSend(
          this.exchange,
          this.routingKey,
          payload,
          m -> {
            headers.forEach((header, value) -> m.getMessageProperties().setHeader(header, value));
            m.getMessageProperties().setHeader(RabbitMQUtils.URL_HEADER, url);
            return m;
          });
    } catch (final AmqpException ex) {
      log.warn("QUEUE_ERROR[webhook] url[{}] headers[{}] payload[{}]", url, headers, payload);
    }
  }
}
