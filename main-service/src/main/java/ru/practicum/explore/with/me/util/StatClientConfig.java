package ru.practicum.explore.with.me.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.stats.client.StatClient;
import ru.practicum.stats.client.StatClientImpl;

@Configuration
public class StatClientConfig {

    @Bean
    public StatClient statClient(@Value("${stat.server-url}") String serverUrl) {
        RestClient client = RestClient.builder()
                .baseUrl(serverUrl)
                .build();

        return new StatClientImpl(client);
    }
}
