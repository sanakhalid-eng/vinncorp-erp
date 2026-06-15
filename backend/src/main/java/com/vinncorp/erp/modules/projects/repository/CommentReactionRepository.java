package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

    Optional<CommentReaction> findByCommentIdAndUserIdAndReactionType(Long commentId, Long userId, String reactionType);

    List<CommentReaction> findByCommentId(Long commentId);

    void deleteByCommentIdAndUserIdAndReactionType(Long commentId, Long userId, String reactionType);

    boolean existsByCommentIdAndUserIdAndReactionType(Long commentId, Long userId, String reactionType);
}



