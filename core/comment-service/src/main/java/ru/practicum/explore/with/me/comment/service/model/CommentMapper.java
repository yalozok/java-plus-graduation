package ru.practicum.explore.with.me.comment.service.model;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentUpdateDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CommentUserDto;
import ru.practicum.explore.with.me.interaction.api.dto.comment.CreateUpdateCommentDto;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "eventDto", ignore = true)
    @Mapping(target = "authorDto", ignore = true)
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    Comment toModel(CreateUpdateCommentDto createUpdateCommentDto);

    @Mapping(target = "eventDto", ignore = true)
    CommentUserDto toUserDto(Comment comment);

    @Mapping(target = "eventDto", ignore = true)
    @Mapping(target = "authorDto", ignore = true)
    CommentUpdateDto toUpdateDto(Comment comment);
}
