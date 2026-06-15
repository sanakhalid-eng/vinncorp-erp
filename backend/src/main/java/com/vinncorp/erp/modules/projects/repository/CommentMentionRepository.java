package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {

    List<CommentMention> findByCommentId(Long commentId);

    void deleteByCommentId(Long commentId);
}



