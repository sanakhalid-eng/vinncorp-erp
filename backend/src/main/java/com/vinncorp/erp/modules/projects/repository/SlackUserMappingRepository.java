package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.SlackUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlackUserMappingRepository extends JpaRepository<SlackUserMapping, Long> {
    List<SlackUserMapping> findBySlackIntegrationId(Long slackIntegrationId);
    Optional<SlackUserMapping> findBySlackIntegrationIdAndSlackUserId(Long slackIntegrationId, String slackUserId);
    Optional<SlackUserMapping> findBySlackIntegrationIdAndUserId(Long slackIntegrationId, Long userId);
    Optional<SlackUserMapping> findBySlackUserId(String slackUserId);
}



