package com.OmObe.OmO.Comment.repository;

import com.OmObe.OmO.Comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    Page<Comment> findByBoard(long boardId, Pageable pageable);
}