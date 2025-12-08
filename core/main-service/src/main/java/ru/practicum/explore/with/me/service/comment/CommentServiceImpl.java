package ru.practicum.explore.with.me.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.explore.with.me.interaction.api.client.user.UserClient;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentUpdateDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentUserDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CreateUpdateCommentDto;
import ru.practicum.explore.with.me.interaction.api.dto.participation.ParticipationRequestStatus;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;
import ru.practicum.explore.with.me.interaction.api.exception.BadRequestException;
import ru.practicum.explore.with.me.interaction.api.exception.ConflictException;
import ru.practicum.explore.with.me.interaction.api.exception.ForbiddenException;
import ru.practicum.explore.with.me.interaction.api.exception.NotFoundException;
import ru.practicum.explore.with.me.mapper.CommentMapper;
import ru.practicum.explore.with.me.model.comment.Comment;

import ru.practicum.explore.with.me.model.event.Event;

import ru.practicum.explore.with.me.repository.CommentRepository;
import ru.practicum.explore.with.me.repository.EventRepository;
import ru.practicum.explore.with.me.repository.ParticipationRequestRepository;
import ru.practicum.explore.with.me.interaction.api.util.ExistenceValidator;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class CommentServiceImpl implements CommentService, ExistenceValidator<Comment> {

    private static final String OBJECT_NOT_FOUND = "Required object was not found.";
    private static final String CONDITIONS_NOT_MET = "Conditions are not met.";

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final ExistenceValidator<Event> eventExistenceValidator;
    private final CommentMapper mapper;


    // admin

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentDto getCommentById(Long id) {
        Comment comment = getOrThrow(id);
        CommentDto result = mapper.toDto(comment);
        UserShortDto userDto = userClient.findById(comment.getAuthorId());
        result.setAuthorDto(userDto);
        return result;
    }

    @Override
    public void deleteCommentByAdmin(Long id) {
        commentRepository.deleteById(id);
    }


    //private

    @Override
    public CommentDto createComment(Long userId, Long eventId, CreateUpdateCommentDto dto) {
        validateText(dto.getText(), 100);

        userClient.findById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        OBJECT_NOT_FOUND, String.format("Event with id: %d was not found", eventId)));

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ConflictException(CONDITIONS_NOT_MET, "Only past events can be commented on");
        }

        if (!requestRepository
                .existsByRequesterIdAndEventIdAndStatus(
                        userId,
                        eventId,
                        ParticipationRequestStatus.CONFIRMED
                )) {
            throw new ConflictException(CONDITIONS_NOT_MET, "Only events the user participated in can be commented on");
        }

        Comment comment = mapper.toModel(dto);
        comment.setAuthorId(userId);
        comment.setEvent(event);

        return mapper.toDto(commentRepository.save(comment));
    }

    @Override
    public CommentUpdateDto updateComment(Long userId, Long commentId, CreateUpdateCommentDto dto) {
        UserShortDto user = userClient.findById(userId);
        validateText(dto.getText(), 1000);

        Comment comment = getOrThrow(commentId);
        if (comment.getAuthorId() != userId) {
            throw new ForbiddenException(CONDITIONS_NOT_MET, "Only author can edit comment");
        }

        comment.setText(dto.getText());
        comment.setUpdatedOn(LocalDateTime.now());
        CommentUpdateDto result = mapper.toUpdateDto(comment);
        result.setAuthorDto(user);
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
        return commentRepository.findByAuthorIdOrderByCreatedOnDesc(userId, pageable)
                .stream()
                .map(mapper::toUserDto)
                .toList();
    }


    //public

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(Long eventId, Pageable pageable) {
        eventExistenceValidator.validateExists(eventId);
        return commentRepository.findByEventIdOrderByCreatedOnDesc(eventId, pageable)
                .stream()
                .map(mapper::toDto)
                .toList();
    }


    private void validateText(String text, int max) {
        if (text == null || text.isBlank() || text.length() > max) {
            throw new BadRequestException("Text param constraint violation.",
                    String.format("Comment text has to be 1–%d symbols", max));
        }
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
