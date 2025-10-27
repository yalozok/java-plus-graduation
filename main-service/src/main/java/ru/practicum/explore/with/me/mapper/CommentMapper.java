package ru.practicum.explore.with.me.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.with.me.model.comment.Comment;
import ru.practicum.explore.with.me.model.comment.CommentDto;
import ru.practicum.explore.with.me.model.comment.CommentUpdateDto;
import ru.practicum.explore.with.me.model.comment.CommentUserDto;
import ru.practicum.explore.with.me.model.comment.CreateUpdateCommentDto;

@Mapper(componentModel = "spring", uses = {UserMapper.class, EventMapper.class})
public interface CommentMapper {
    @Mapping(target = "eventDto", source = "event")
    @Mapping(target = "authorDto", source = "author")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "author", ignore = true)
    Comment toModel(CreateUpdateCommentDto createUpdateCommentDto);

    @Mapping(target = "eventDto", source = "event")
    CommentUserDto toUserDto(Comment comment);

    @Mapping(target = "eventDto", source = "event")
    @Mapping(target = "authorDto", source = "author")
    CommentUpdateDto toUpdateDto(Comment comment);
}
