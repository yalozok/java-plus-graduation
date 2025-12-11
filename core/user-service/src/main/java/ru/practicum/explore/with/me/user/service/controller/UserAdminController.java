package ru.practicum.explore.with.me.user.service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.with.me.interaction.api.dto.user.AdminUserFindParam;
import ru.practicum.explore.with.me.interaction.api.dto.user.NewUserRequest;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.user.service.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
@Validated
public class UserAdminController {
    private final UserService service;

    @GetMapping
    @Loggable
    public List<UserDto> find(@RequestParam(required = false)
                              List<Long> ids,
                              @RequestParam(defaultValue = "0")
                              @PositiveOrZero(message = "must be positive or zero")
                              int from,
                              @RequestParam(defaultValue = "10")
                              @Positive(message = "must be positive")
                              int size) {
        AdminUserFindParam param = AdminUserFindParam.builder()
                .ids(ids)
                .from(from)
                .size(size)
                .build();
        return service.find(param);
    }

    @PostMapping
    @Loggable
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody
                          @Valid
                          NewUserRequest newUserRequest) {
        return service.create(newUserRequest);
    }

    @DeleteMapping("/{userId}")
    @Loggable
    public void delete(@PathVariable
                       @Positive(message = "must be positive")
                       Long userId) {
        service.delete(userId);
    }

    @GetMapping("/{id}")
    @Loggable
    public UserShortDto findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
