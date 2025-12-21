package ru.practicum.explore.with.me.comment.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.with.me.comment.service.model.Comment;
import ru.practicum.explore.with.me.comment.service.model.CommentRepository;
import ru.practicum.explore.with.me.interaction.api.exception.NotFoundException;
import ru.practicum.explore.with.me.interaction.api.util.ExistenceValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentTransactionalService implements ExistenceValidator<Comment> {
    private final CommentRepository commentRepository;
    private static final String OBJECT_NOT_FOUND = "Required object was not found.";

    @Transactional(readOnly = true)
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(OBJECT_NOT_FOUND,
                        String.format("Comment with id: %d was not found", id)));
    }

    @Transactional
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByUserIdOrderDesc(Long userId, Pageable pageable) {
        return commentRepository.findByAuthorIdOrderByCreatedOnDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByEventIdOrderDesc(Long userId, Pageable pageable) {
        return commentRepository.findByEventIdOrderByCreatedOnDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateExists(Long id) {
        if (commentRepository.findById(id).isEmpty()) {
            throw new NotFoundException(OBJECT_NOT_FOUND, "Comment with id=" + id + " was not found");
        }
    }
}
