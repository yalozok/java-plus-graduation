package ru.practicum.explore.with.me.comment.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.explore.with.me.comment",
        "ru.practicum.explore.with.me.logging"
})
@EnableFeignClients(basePackages = {
        "ru.practicum.explore.with.me.interaction.api.client"
})
@EnableAspectJAutoProxy
public class CommentServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CommentServiceApp.class, args);
    }
}