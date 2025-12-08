package ru.practicum.explore.with.me.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentUpdateDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentUserDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CreateUpdateCommentDto;
import ru.practicum.explore.with.me.model.comment.Comment;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CommentMapper {
    @Mapping(target = "eventDto", source = "event")
    @Mapping(target = "authorDto", ignore = true)
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    Comment toModel(CreateUpdateCommentDto createUpdateCommentDto);

    @Mapping(target = "eventDto", source = "event")
    CommentUserDto toUserDto(Comment comment);

    @Mapping(target = "eventDto", source = "event")
    @Mapping(target = "authorDto", ignore = true)
    CommentUpdateDto toUpdateDto(Comment comment);
}
