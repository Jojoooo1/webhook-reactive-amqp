package com.service.webhook.utils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Slf4j
@UtilityClass
public class WebClientUtils {

  public static WebClient createWebClient(
      final int timeOutInMs, final ExchangeFilterFunction errorHandler) {

    final WebClient.Builder builder =
        WebClient.builder()
            .clientConnector(
                new ReactorClientHttpConnector(createHttpClientWithProvider(timeOutInMs)));

    if (errorHandler != null) {
      builder.filters(exchangeFilterFunctions -> exchangeFilterFunctions.add(errorHandler));
    }

    return builder.build();
  }

  public static HttpClient createHttpClientWithProvider(final int timeOutInMs) {

    final ConnectionProvider provider =
        ConnectionProvider.builder("webhook-http")
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(20)) // default is not set
            .maxLifeTime(Duration.ofSeconds(60)) // default is not set
            .pendingAcquireTimeout(Duration.ofSeconds(45)) // default
            .evictInBackground(Duration.ofSeconds(120)) // default is 0 (disabled)
            .build();

    return HttpClient.create()
        .compress(true)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeOutInMs) // connection timeout
        .responseTimeout(Duration.ofMillis(timeOutInMs)) // response timeout for all requests
        .doOnConnected(
            conn ->
                conn.addHandlerLast(
                        new ReadTimeoutHandler(timeOutInMs, TimeUnit.MILLISECONDS)) // read timeout
                    .addHandlerLast(
                        new WriteTimeoutHandler(
                            timeOutInMs, TimeUnit.MILLISECONDS)) // write timeout
            );
  }
}
