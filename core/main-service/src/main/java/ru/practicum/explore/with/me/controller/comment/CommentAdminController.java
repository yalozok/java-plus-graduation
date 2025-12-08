package ru.practicum.explore.with.me.controller.comment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentDto;
import ru.practicum.explore.with.me.logging.Loggable;
import ru.practicum.explore.with.me.service.comment.CommentService;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
public class CommentAdminController {
    private final CommentService commentService;

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    @Loggable
    public CommentDto getCommentById(@PathVariable @NotNull @PositiveOrZero Long commentId) {
        return commentService.getCommentById(commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Loggable
    public void deleteCommentById(@PathVariable @NotNull @PositiveOrZero Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }
}
