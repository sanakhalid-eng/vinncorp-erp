package com.vinncorp.erp.modules.crm.repository;

import com.vinncorp.erp.modules.crm.entity.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {
    List<Pipeline> findAllByWorkspaceIdOrderByDisplayOrderAsc(Long workspaceId);
    Optional<Pipeline> findByWorkspaceIdAndIsDefaultTrue(Long workspaceId);
    Optional<Pipeline> findByIdAndWorkspaceId(Long id, Long workspaceId);
    boolean existsByNameAndWorkspaceId(String name, Long workspaceId);
}
