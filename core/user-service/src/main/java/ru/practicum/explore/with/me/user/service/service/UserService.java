package ru.practicum.explore.with.me.user.service.service;


import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.explore.with.me.interaction.api.dto.user.AdminUserFindParam;
import ru.practicum.explore.with.me.interaction.api.dto.user.NewUserRequest;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;

import java.util.List;

public interface UserService {
    List<UserDto> find(AdminUserFindParam param);

    UserDto create(NewUserRequest newUserRequest);

    void delete(Long userId);

    UserShortDto findById(Long id);
}
