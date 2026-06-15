package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ProjectTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplateEntity, Long> {

    List<ProjectTemplateEntity> findByIsActiveTrue();

    List<ProjectTemplateEntity> findByCategory(String category);
}



