package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.SlackIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlackIntegrationRepository extends JpaRepository<SlackIntegration, Long> {
    List<SlackIntegration> findByProjectIdAndIsActiveTrue(Long projectId);
    Optional<SlackIntegration> findByWorkspaceId(String workspaceId);
    Optional<SlackIntegration> findByChannelId(String channelId);
}



