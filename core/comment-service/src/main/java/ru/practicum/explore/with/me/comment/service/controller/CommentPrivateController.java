package ru.practicum.explore.with.me.comment.service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentUpdateDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentUserDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CreateUpdateCommentDto;
import ru.practicum.explore.with.me.interaction.api.exception.BadRequestException;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.comment.service.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
public class CommentPrivateController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Loggable
    public CommentDto createComment(@PathVariable @NotNull @PositiveOrZero Long userId,
                                    @RequestParam @NotNull @PositiveOrZero Long eventId,
                                    @RequestBody @Valid CreateUpdateCommentDto commentDto) {
        validateText(commentDto.getText(), 100);
        return commentService.createComment(userId, eventId, commentDto);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    @Loggable
    public CommentUpdateDto updateComment(@PathVariable @NotNull @PositiveOrZero Long userId,
                                          @PathVariable @NotNull @PositiveOrZero Long commentId,
                                          @RequestBody @Valid CreateUpdateCommentDto commentDto) {
        validateText(commentDto.getText(), 1000);
        return commentService.updateComment(userId, commentId, commentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Loggable
    public void deleteComment(@PathVariable @NotNull @PositiveOrZero Long userId,
                              @PathVariable @NotNull @PositiveOrZero Long commentId) {
        commentService.deleteCommentByAuthor(userId, commentId);
    }

    @GetMapping
    @Loggable
    public List<CommentUserDto> getCommentsByUser(@PathVariable @NotNull @PositiveOrZero Long userId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.getCommentsByAuthor(userId, PageRequest.of(from / size, size));
    }

    private void validateText(String text, int max) {
        if (text == null || text.isBlank() || text.length() > max) {
            throw new BadRequestException("Text param constraint violation.",
                    String.format("Comment text has to be 1–%d symbols", max));
        }
    }
}
