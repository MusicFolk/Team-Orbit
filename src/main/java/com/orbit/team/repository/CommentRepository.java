package com.orbit.team.repository;

import com.orbit.team.entity.Comment;
import com.orbit.team.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEventOrderByCreatedAtAsc(Event event);
}