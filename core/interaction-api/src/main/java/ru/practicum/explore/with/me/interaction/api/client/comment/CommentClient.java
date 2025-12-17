package ru.practicum.explore.with.me.interaction.api.client.comment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentDto;

import java.util.List;

@FeignClient(name = "comment-service",
        path = "/admin/comments",
        configuration = CommentFeignConfig.class,
        fallback = CommentFeignClientFallback.class)
public interface CommentClient {

    @GetMapping("/by-event/{eventId}")
    List<CommentDto> getCommentsByEvent(@PathVariable @NotNull @PositiveOrZero Long eventId,
                                        PageRequest pageRequest);
}
