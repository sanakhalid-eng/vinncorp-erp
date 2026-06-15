package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findByBoardIdOrderByColumnOrder(Long boardId);
}


