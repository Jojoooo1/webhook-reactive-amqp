package com.service.webhook.listeners;

import com.service.webhook.clients.WebhookReactiveClient;
import com.service.webhook.utils.RabbitMQUtils;
import com.service.webhook.utils.UrlUtils;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookListenerService {

  private final WebhookReactiveClient reactiveClient;

  @RabbitListener(queues = "${service.webhooks.template.queue}", messageConverter = "jsonConverter")
  public Mono<Void> process(final Message<LinkedHashMap<String, Object>> message) {
    try {

      final String url = RabbitMQUtils.getUrl(message);

      if (!UrlUtils.isValid(url)) {
        log.info("[ERROR] url[{}] is invalid, dropping message [{}]", url, message.getPayload());
        return Mono.empty();
      }

      return this.reactiveClient.sendWebhook(url, message);
    } catch (final Exception ex) {
      log.error(String.format("[ERROR] '%s'", ex.getMessage()));
      return Mono.empty();
    }
  }
}
