package ru.practicum.explore.with.me.comment.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.explore.with.me.interaction.api.client.event.EventClient;
import ru.practicum.explore.with.me.interaction.api.client.request.RequestClient;
import ru.practicum.explore.with.me.interaction.api.client.user.UserClient;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentUpdateDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentUserDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CreateUpdateCommentDto;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventFullDto;
import ru.practicum.explore.with.me.interaction.api.dto.event.EventShortDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserDto;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;
import ru.practicum.explore.with.me.interaction.api.exception.BadRequestException;
import ru.practicum.explore.with.me.interaction.api.exception.ConflictException;
import ru.practicum.explore.with.me.interaction.api.exception.ForbiddenException;
import ru.practicum.explore.with.me.interaction.api.exception.NotFoundException;
import ru.practicum.explore.with.me.comment.service.model.CommentMapper;
import ru.practicum.explore.with.me.comment.service.model.Comment;
import ru.practicum.explore.with.me.comment.service.model.CommentRepository;
import ru.practicum.explore.with.me.interaction.api.util.ExistenceValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class CommentServiceImpl implements CommentService, ExistenceValidator<Comment> {

    private static final String OBJECT_NOT_FOUND = "Required object was not found.";
    private static final String CONDITIONS_NOT_MET = "Conditions are not met.";

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestClient requestClient;
    private final CommentMapper mapper;


    // admin

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(Long id) {
        Comment comment = getOrThrow(id);
        CommentDto result = mapper.toDto(comment);
        UserShortDto userDto = userClient.findById(comment.getAuthorId());
        result.setAuthorDto(userDto);

        EventFullDto event = eventClient.getEventById(comment.getEventId());
        result.setEventDto(new CommentDto.CommentEventDto(event.getId(), event.getTitle()));
        return result;
    }

    @Override
    public void deleteCommentByAdmin(Long id) {
        commentRepository.deleteById(id);
    }


    //private

    @Override
    public CommentDto createComment(Long userId, Long eventId, CreateUpdateCommentDto dto) {
        UserShortDto user = userClient.findById(userId);
        EventFullDto event = eventClient.getEventById(eventId);

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ConflictException(CONDITIONS_NOT_MET, "Only past events can be commented on");
        }

        if (!requestClient.isParticipantApproved(userId, eventId)) {
            throw new ConflictException(CONDITIONS_NOT_MET, "Only events the user participated in can be commented on");
        }

        Comment comment = mapper.toModel(dto);
        comment.setAuthorId(userId);
        comment.setEventId(eventId);
        CommentDto result = mapper.toDto(commentRepository.save(comment));
        result.setAuthorDto(user);
        result.setEventDto(new CommentDto.CommentEventDto(event.getId(), event.getTitle()));
        return result;
    }

    @Override
    public CommentUpdateDto updateComment(Long userId, Long commentId, CreateUpdateCommentDto dto) {
        UserShortDto user = userClient.findById(userId);
        Comment comment = getOrThrow(commentId);
        if (comment.getAuthorId() != userId) {
            throw new ForbiddenException(CONDITIONS_NOT_MET, "Only author can edit comment");
        }

        comment.setText(dto.getText());
        comment.setUpdatedOn(LocalDateTime.now());
        CommentUpdateDto result = mapper.toUpdateDto(comment);
        result.setAuthorDto(user);

        EventFullDto event = eventClient.getEventById(comment.getEventId());
        result.setEventDto(new CommentUpdateDto.CommentEventDto(event.getId(), event.getTitle()));
        return result;
    }

    @Override
    public void deleteCommentByAuthor(Long userId, Long commentId) {
        userClient.findById(userId);
        Comment comment = getOrThrow(commentId);

        if (comment.getAuthorId() != userId) {
            throw new ForbiddenException(CONDITIONS_NOT_MET, "Only author / admin can delete comment");
        }
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentUserDto> getCommentsByAuthor(Long userId, Pageable pageable) {
        userClient.findById(userId);
        List<Comment> comments = commentRepository.findByAuthorIdOrderByCreatedOnDesc(userId, pageable);

        List<Long> eventIds = comments.stream().map(Comment::getEventId).toList();
        Map<Long, EventShortDto> eventsMap = eventClient.getEventsByIds(eventIds)
                .stream()
                .collect(Collectors.toMap(EventShortDto::getId, Function.identity()));
        return comments.stream()
                .map(comment -> {
                    CommentUserDto userComment = mapper.toUserDto(comment);
                    EventShortDto event = eventsMap.get(comment.getEventId());
                    userComment.setEventDto(new CommentUserDto.CommentEventDto(event.getId(), event.getTitle()));
                    return userComment;
                })
                .toList();
    }

    //public

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(Long eventId, Pageable pageable) {
        EventFullDto event = eventClient.getEventById(eventId);
        List<Comment> comments = commentRepository.findByEventIdOrderByCreatedOnDesc(eventId, pageable);

        List<Long> userIds = comments.stream().map(Comment::getAuthorId).toList();
        Map<Long, UserDto> usersMap = userClient.find(userIds, 0, userIds.size())
                .stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return comments.stream()
                .map(comment -> {
                    CommentDto result = mapper.toDto(comment);
                    result.setEventDto(new CommentDto.CommentEventDto(event.getId(), event.getTitle()));
                    UserDto userDto = usersMap.get(comment.getAuthorId());
                    result.setAuthorDto(new UserShortDto(userDto.getId(), userDto.getName()));
                    return result;
                })
                .toList();
    }

    private Comment getOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(OBJECT_NOT_FOUND,
                        String.format("Comment with id: %d was not found", id)));
    }

    @Override
    public void validateExists(Long id) {
        if (commentRepository.findById(id).isEmpty()) {
            throw new NotFoundException(OBJECT_NOT_FOUND, "Comment with id=" + id + " was not found");
        }
    }
}
