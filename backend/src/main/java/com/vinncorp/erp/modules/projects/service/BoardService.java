package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.BoardColumnResponse;
import com.vinncorp.erp.modules.projects.dto.response.BoardResponse;

import java.util.List;

public interface BoardService {
    BoardResponse getBoardByProjectId(Long projectId);
    BoardResponse createBoardForProject(Long projectId, String name);
    BoardResponse updateColumnOrder(Long boardId, List<Long> columnIds);
    BoardColumnResponse addColumn(Long boardId, String name);
    BoardColumnResponse updateColumn(Long columnId, String name);
    void deleteColumn(Long columnId);
}


