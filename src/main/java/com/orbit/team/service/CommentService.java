package com.orbit.team.service;

import com.orbit.team.entity.Comment;

import java.util.List;

public interface CommentService {

    Comment addComment(Comment comment);

    List<Comment> getCommentsForEvent(Long eventId);

    void deleteComment(Long commentId, Long userId);
}