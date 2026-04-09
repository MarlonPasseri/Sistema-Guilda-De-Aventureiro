package br.com.guilda.registro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class ElasticsearchApiConfig {

    @Bean
    public RestClient elasticsearchApiClient(
        @Value("${spring.elasticsearch.uris:http://localhost:9200}") String elasticsearchUri
    ) {
        return RestClient.builder()
            .baseUrl(elasticsearchUri)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
