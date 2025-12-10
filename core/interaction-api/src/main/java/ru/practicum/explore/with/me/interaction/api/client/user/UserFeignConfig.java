package ru.practicum.explore.with.me.interaction.api.client.user;

import feign.Feign;
import feign.Logger;
import org.springframework.context.annotation.Bean;

public class UserFeignConfig {
    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .errorDecoder(new CustomErrorDecoder())
                .logLevel(Logger.Level.FULL);
    }
}
