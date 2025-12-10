package ru.practicum.explore.with.me.interaction.api.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.explore.with.me.interaction.api.contract.UserOperations;

@FeignClient(name = "user-service",
        path = "/admin/users",
        configuration = UserFeignConfig.class)
public interface UserClient extends UserOperations {
}
