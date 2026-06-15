package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByProjectId(Long projectId);
}


