package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    List<TaskAttachment> findByTaskIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long taskId);

    Optional<TaskAttachment> findByIdAndDeletedAtIsNull(Long id);
}



