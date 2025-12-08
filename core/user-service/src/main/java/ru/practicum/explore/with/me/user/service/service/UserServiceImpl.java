package ru.practicum.explore.with.me.user.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.with.me.interaction.api.dto.user.AdminUserFindParam;
import ru.practicum.explore.with.me.interaction.api.dto.user.NewUserRequest;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;
import ru.practicum.explore.with.me.interaction.api.exception.ConflictException;
import ru.practicum.explore.with.me.interaction.api.exception.NotFoundException;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.user.service.model.UserMapper;
import ru.practicum.explore.with.me.user.service.model.User;
import ru.practicum.explore.with.me.user.service.model.UserRepository;
import ru.practicum.explore.with.me.interaction.api.util.DataProvider;
import ru.practicum.explore.with.me.interaction.api.util.ExistenceValidator;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService, ExistenceValidator<User>, DataProvider<UserShortDto, User> {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Loggable
    public List<UserDto> find(AdminUserFindParam param) {
        List<UserDto> result;

        if (param.getIds() != null && !param.getIds().isEmpty()) {
            result = userRepository.findByIdIn(param.getIds()).stream()
                    .map(this::mapUserDto)
                    .toList();
        } else {
            PageRequest pageRequest = PageRequest.of(param.getFrom(), param.getSize());
            result = userRepository.findAll(pageRequest).get()
                    .map(this::mapUserDto)
                    .toList();
        }
        return result;
    }

    @Loggable
    @Override
    public UserShortDto findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException(
                "The required object was not found.",
                "User with id=" + id + " was not found"
        ));

        return new UserShortDto(user.getId(), user.getName());
    }

    @Transactional
    @Override
    @Loggable
    public UserDto create(NewUserRequest newUserRequest) {
        validateEmailUnique(newUserRequest.getEmail());
        return mapUserDto(userRepository.save(mapEntity(newUserRequest)));
    }

    @Transactional
    @Override
    @Loggable
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    private User mapEntity(NewUserRequest newUserRequest) {
        return userMapper.toEntity(newUserRequest);
    }

    private UserDto mapUserDto(User user) {
        return userMapper.toDto(user);
    }

    @Override
    public UserShortDto getDto(User entity) {
        return userMapper.toShortDto(entity);
    }

    @Override
    @Loggable
    public void validateExists(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new NotFoundException("The required object was not found.",
                    "User with id=" + id + " was not found");
        }
    }

    @Loggable
    private void validateEmailUnique(String email) {
        if (userRepository.isExistsEmail(email)) {
            throw new ConflictException("The email of user should be unique.",
                    "User with email=" + email + " is already exist");
        }
    }
}
