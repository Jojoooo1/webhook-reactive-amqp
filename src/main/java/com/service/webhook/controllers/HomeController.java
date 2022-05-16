package com.service.webhook.controllers;

import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@Slf4j
@RestController
public class HomeController {

  @PostMapping("/ok")
  public ResponseEntity<String> create(@RequestBody final HashMap<String, Object> request) {
    log.info(String.valueOf(request));
    return ResponseEntity.ok().build();
  }

  @PostMapping("/body")
  public ResponseEntity<String> body() {
    final String json = "{ \"response\" : \"response\" }";
    return ResponseEntity.ok(json);
  }

  // errors

  @PostMapping("/sleep")
  public ResponseEntity<String> sleep() throws InterruptedException {
    Thread.sleep(15000);
    return ResponseEntity.ok().build();
  }

  // 300+ (success)

  @PostMapping("/found")
  public ResponseEntity<String> found() {
    return ResponseEntity.status(HttpStatus.FOUND).build();
  }

  // 400+ (skip retry)

  @PostMapping("/too-early")
  public ResponseEntity<String> tooEarly() {
    return ResponseEntity.status(HttpStatus.TOO_EARLY).build();
  }

  @PostMapping("/not-authorized")
  public ResponseEntity<String> notAuthorized() {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  // 500+ (retry)

  @PostMapping("/bad-gateway")
  public ResponseEntity<String> badGateway() {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
  }

  @PostMapping("/internal-error")
  public ResponseEntity<String> internalError() {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}
