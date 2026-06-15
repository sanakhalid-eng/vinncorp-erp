package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.parentComment WHERE c.task.id = :taskId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findTopLevelCommentsByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT c FROM Comment c WHERE c.task.id = :taskId ORDER BY c.createdAt ASC")
    List<Comment> findAllByTaskIdOrdered(@Param("taskId") Long taskId);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.reactions LEFT JOIN FETCH c.user WHERE c.task.id = :taskId AND c.parentComment IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsWithReactions(@Param("taskId") Long taskId);

    Page<Comment> findByTaskId(Long taskId, Pageable pageable);
}



