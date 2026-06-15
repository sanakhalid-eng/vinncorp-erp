package com.vinncorp.erp.modules.hr.repository;

import com.vinncorp.erp.modules.hr.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    Optional<Designation> findByIdAndWorkspaceId(Long id, Long workspaceId);

    List<Designation> findAllByWorkspaceId(Long workspaceId);

    List<Designation> findAllByWorkspaceIdAndActiveTrue(Long workspaceId);

    Optional<Designation> findByTitleAndWorkspaceId(String title, Long workspaceId);

    boolean existsByTitleAndWorkspaceId(String title, Long workspaceId);
}


