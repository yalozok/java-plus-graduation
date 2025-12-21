package ru.practicum.explore.with.me.request.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.explore.with.me.request",
        "ru.practicum.explore.with.me.logging"})
@EnableFeignClients(basePackages = {
        "ru.practicum.explore.with.me.interaction.api.client"
})
@EnableAspectJAutoProxy
public class RequestServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApp.class, args);
    }
}
