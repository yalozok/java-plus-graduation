package ru.practicum.explore.with.me.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.with.me.interaction.api.dto.user.NewUserRequest;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;
import ru.practicum.explore.with.me.model.user.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User entity);

    UserShortDto toShortDto(User entity);

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest newUserRequest);
}