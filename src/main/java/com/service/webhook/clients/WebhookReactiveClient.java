package com.service.webhook.clients;

import com.service.webhook.exceptions.WebhookException;
import com.service.webhook.services.WebhookTemplateService;
import com.service.webhook.utils.JsonUtils;
import com.service.webhook.utils.RabbitMQUtils;
import com.service.webhook.utils.WebClientUtils;
import java.util.HashMap;
import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class WebhookReactiveClient {

  private final WebClient webClient;
  private final WebhookTemplateService webhookTemplateService;

  @Autowired
  public WebhookReactiveClient(
      @Value("${service.webhooks.timeout}") final Integer timeOut,
      final WebhookTemplateService webhookTemplateService) {
    this.webhookTemplateService = webhookTemplateService;
    this.webClient = WebClientUtils.createWebClient(timeOut, null);
  }

  public Mono<Void> sendWebhook(
      final String url, final Message<LinkedHashMap<String, Object>> message) {

    final String requestBody = JsonUtils.toJSON(message.getPayload());
    final HashMap<String, String> headers = RabbitMQUtils.getHeaders(message);
    final Integer retryCount = RabbitMQUtils.getRetryCount(headers);

    if (retryCount == 0) {
      log.info("HTTP_REQUEST[webhook] url[{}] - body[{}]", url, requestBody);
    } else {
      log.info(
          "HTTP_REQUEST[webhook] url[{}] - body[{}] with retryCount[{}]", url, requestBody, retryCount);
    }

    return this.webClient
        .post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders -> headers.forEach(httpHeaders::add))
        .bodyValue(requestBody)
        .exchangeToMono(
            response ->
                response
                    .bodyToMono(String.class)
                    // Note: Inside flatMap subscribe to a new Mono this is why we need to set the
                    // thread.
                    .subscribeOn(Schedulers.boundedElastic())
                    .defaultIfEmpty(StringUtils.EMPTY)
                    .flatMap(
                        body -> {
                          final HttpStatus status = response.statusCode();
                          if (status.isError()) {
                            if (status.is4xxClientError()) {
                              log.info(
                                  "HTTP_STATUS_ERROR[webhook] is4xxClientError skipping retry. status[{}] - body[{}]",
                                  status,
                                  body);
                              return Mono.empty();
                            } else {
                              return Mono.error(
                                  new WebhookException(status, body));
                            }

                          } else {
                            log.info(
                                "HTTP_SUCCESS[webhook] status[{}] body[{}]",
                                status,
                                body);
                            return Mono.empty();
                          }
                        }))
        .onErrorResume(
            error -> {
              if (error instanceof final WebhookException webhookException) {
                log.info(
                    "HTTP_ERROR[webhook] errorStatus['{}'] - errorBody[{}]",
                    webhookException.getHttpStatus(),
                    webhookException.getErrorMessage());

              } else {
                log.info("HTTP_ERROR[webhook] errorDetails['{}']", error.getMessage());
              }

              // Note: needs to be requeued using linkedHashMap or converter will throw an error.
               return Mono.fromRunnable(
                      () -> this.webhookTemplateService.send(url, headers, message.getPayload()))
                  .subscribeOn(Schedulers.boundedElastic());
            })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }
}
