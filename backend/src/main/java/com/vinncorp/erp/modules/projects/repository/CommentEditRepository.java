package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.CommentEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentEditRepository extends JpaRepository<CommentEdit, Long> {

    List<CommentEdit> findByCommentIdOrderByEditedAtAsc(Long commentId);
}



