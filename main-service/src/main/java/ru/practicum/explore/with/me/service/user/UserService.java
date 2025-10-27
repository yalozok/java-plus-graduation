package ru.practicum.explore.with.me.service.user;

import ru.practicum.explore.with.me.model.user.AdminUserFindParam;
import ru.practicum.explore.with.me.model.user.NewUserRequest;
import ru.practicum.explore.with.me.model.user.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> find(AdminUserFindParam param);

    UserDto create(NewUserRequest newUserRequest);

    void delete(Long userId);
}
