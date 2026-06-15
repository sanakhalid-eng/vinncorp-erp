package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.CommandPaletteRecent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandPaletteRecentRepository extends JpaRepository<CommandPaletteRecent, Long> {

    List<CommandPaletteRecent> findTop10ByWorkspaceIdAndUserIdAndDeletedAtIsNullOrderByUsedAtDesc(
            Long workspaceId, Long userId);
}



