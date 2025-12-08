package ru.practicum.explore.with.me.interaction.api.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.explore.with.me.interaction.api.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private long id;
    private String text;
    private UserShortDto authorDto;
    private CommentEventDto eventDto;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    public static record CommentEventDto(long id, String title) {
    }
}
