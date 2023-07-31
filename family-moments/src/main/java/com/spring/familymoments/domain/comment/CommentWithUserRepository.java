package com.spring.familymoments.domain.comment;

import com.spring.familymoments.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentWithUserRepository extends JpaRepository<Comment, Long> {
    //유저가 쓴 모든 댓글들 조회
    @Query("SELECT c FROM Comment c WHERE c.writer.userId = :userId")
    List<Comment> findCommentsByUserId(Long userId);
}