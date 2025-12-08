package ru.practicum.explore.with.me.interaction.api.dto.comment;

import lombok.Data;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
public class CommentUpdateDto {
    private long id;
    private String text;
    private UserShortDto authorDto;
    private CommentEventDto eventDto;
    private LocalDateTime updatedOn;

    public static record CommentEventDto(long id, String title) {
    }
}
