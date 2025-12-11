package ru.practicum.explore.with.me.interaction.api.client.comment;

import feign.Feign;
import feign.Logger;
import org.springframework.context.annotation.Bean;

public class CommentFeignConfig {
    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .errorDecoder(new CustomErrorDecoder())
                .logLevel(Logger.Level.FULL);
    }
}
