package com.smf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class SupabaseClientConfig {

  @Bean
  RestClient supabaseRestClient(
      @Value("${supabase.url}") String baseUrl,
      @Value("${supabase.secret-key}") String secretKey) {

    return RestClient.builder()
        .baseUrl(baseUrl + "/rest/v1")
        .defaultHeader("apikey", secretKey)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("Prefer", "return=representation")
        .build();
  }
}
