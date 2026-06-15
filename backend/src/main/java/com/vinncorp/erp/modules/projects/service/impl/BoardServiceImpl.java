package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.response.BoardColumnResponse;
import com.vinncorp.erp.modules.projects.dto.response.BoardResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;
import com.vinncorp.erp.modules.projects.entity.Board;
import com.vinncorp.erp.modules.projects.entity.BoardColumn;
import com.vinncorp.erp.modules.projects.mapper.TaskMapper;
import com.vinncorp.erp.modules.projects.repository.BoardColumnRepository;
import com.vinncorp.erp.modules.projects.repository.BoardRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.service.BoardService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final ProjectRepository projectRepository;

    @Override
    public BoardResponse getBoardByProjectId(Long projectId) {
        Board board = boardRepository.findByProjectId(projectId)
                .orElseGet(() -> null);

        if (board == null) {
            return null;
        }

        return mapToBoardResponse(board);
    }

    @Override
    @Transactional
    public BoardResponse createBoardForProject(Long projectId, String name) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (boardRepository.findByProjectId(projectId).isPresent()) {
            throw new IllegalStateException("Board already exists for this project");
        }

        Board board = new Board();
        board.setName(name);
        board.setProject(project);
        board.setColumns(new ArrayList<>());

        board = boardRepository.save(board);

        List<BoardColumn> defaultColumns = List.of(
            createColumn("To Do", 0, board),
            createColumn("In Progress", 1, board),
            createColumn("Done", 2, board)
        );

        board.setColumns(defaultColumns);
        board = boardRepository.save(board);

        return mapToBoardResponse(board);
    }

    private BoardColumn createColumn(String name, int order, Board board) {
        BoardColumn column = new BoardColumn();
        column.setName(name);
        column.setColumnOrder(order);
        column.setBoard(board);
        column.setTasks(new ArrayList<>());
        return boardColumnRepository.save(column);
    }

    @Override
    @Transactional
    public BoardResponse updateColumnOrder(Long boardId, List<Long> columnIds) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        List<BoardColumn> columns = board.getColumns();
        for (int i = 0; i < columnIds.size(); i++) {
            Long columnId = columnIds.get(i);
            BoardColumn column = columns.stream()
                    .filter(c -> c.getId().equals(columnId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Column not found"));
            column.setColumnOrder(i);
        }

        board = boardRepository.save(board);
        return mapToBoardResponse(board);
    }

    @Override
    @Transactional
    public BoardColumnResponse addColumn(Long boardId, String name) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        int maxOrder = board.getColumns().stream()
                .mapToInt(BoardColumn::getColumnOrder)
                .max()
                .orElse(-1);

        BoardColumn column = new BoardColumn();
        column.setName(name);
        column.setColumnOrder(maxOrder + 1);
        column.setBoard(board);
        column.setTasks(new ArrayList<>());

        column = boardColumnRepository.save(column);

        return mapToColumnResponse(column);
    }

    @Override
    @Transactional
    public void deleteColumn(Long columnId) {
        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));

        if (!column.getTasks().isEmpty()) {
            throw new IllegalStateException("Cannot delete column with tasks. Move tasks first.");
        }

        boardColumnRepository.delete(column);
    }

    @Override
    @Transactional
    public BoardColumnResponse updateColumn(Long columnId, String name) {
        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));

        column.setName(name);
        column = boardColumnRepository.save(column);

        return mapToColumnResponse(column);
    }

    private BoardResponse mapToBoardResponse(Board board) {
        List<BoardColumnResponse> columnResponses = board.getColumns().stream()
                .sorted((a, b) -> a.getColumnOrder().compareTo(b.getColumnOrder()))
                .map(this::mapToColumnResponse)
                .toList();

        return BoardResponse.builder()
                .id(board.getId())
                .name(board.getName())
                .projectId(board.getProject().getId())
                .columns(columnResponses)
                .build();
    }

    private BoardColumnResponse mapToColumnResponse(BoardColumn column) {
        List<TaskResponse> taskResponses = column.getTasks().stream()
                .sorted((a, b) -> {
                    if (a.getPosition() == null && b.getPosition() == null) return 0;
                    if (a.getPosition() == null) return 1;
                    if (b.getPosition() == null) return -1;
                    return a.getPosition().compareTo(b.getPosition());
                })
                .map(TaskMapper::toResponse)
                .toList();

        return BoardColumnResponse.builder()
                .id(column.getId())
                .name(column.getName())
                .order(column.getColumnOrder())
                .tasks(taskResponses)
                .build();
    }
}


