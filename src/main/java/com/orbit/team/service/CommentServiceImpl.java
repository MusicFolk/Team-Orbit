package com.orbit.team.service;

import com.orbit.team.entity.Comment;
import com.orbit.team.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public Comment addComment(Comment comment) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be blank");
        }

        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getCommentsForEvent(Long eventId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}