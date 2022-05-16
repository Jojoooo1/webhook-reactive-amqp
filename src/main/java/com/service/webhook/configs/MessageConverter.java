package com.service.webhook.configs;

// Note: keep for future modification
// Warning: if uncomment rabbitmq listener will inject it automatically

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConverter {

  // Note 1: Message needs to be of type JSON or will throw error.
  // Note 2: Message payload will be of type LinkedHashMap

  @Bean
  public Jackson2JsonMessageConverter jsonConverter() {

    return new JacksonMessageConverter(null);
    // Note: can use specific mapper if needed.
    //    return new JacksonMessageConverter(
    //        new ObjectMapper()
    //            .setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS)
    //            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
  }

  public static class JacksonMessageConverter extends Jackson2JsonMessageConverter {

    public JacksonMessageConverter(final ObjectMapper mapper) {
      super();
    }

    @SneakyThrows
    @Override
    public Object fromMessage(final Message message) {
      message.getMessageProperties().setContentType("application/json");
      return super.fromMessage(message);
    }
  }
}
