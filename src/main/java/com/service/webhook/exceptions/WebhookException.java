package com.service.webhook.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WebhookException extends RuntimeException {

  private static final long serialVersionUID = -3154618962130084535L;
  private final HttpStatus httpStatus;
  private final String errorMessage;

  public WebhookException(final HttpStatus httpStatus, final String errorMessage) {
    super();
    this.httpStatus = httpStatus;
    this.errorMessage = errorMessage;
  }
}
