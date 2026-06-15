package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.BoardColumnResponse;
import com.vinncorp.erp.modules.projects.dto.response.BoardResponse;
import com.vinncorp.erp.modules.projects.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Tag(name = "Projects")
public class BoardController {

    private final BoardService boardService;

    @PreAuthorize("hasAuthority('VIEW_PROJECT')")
    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get board by project", description = "Retrieve the board for a specific project")
    public ResponseEntity<ApiResponse<BoardResponse>> getBoardByProject(@PathVariable Long projectId) {
        BoardResponse board = boardService.getBoardByProjectId(projectId);
        if (board == null) {
            return ResponseEntity.ok(new ApiResponse<>(true, "No board found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Board fetched successfully", board));
    }

    @PreAuthorize("hasAuthority('CREATE_BOARD')")
    @PostMapping("/project/{projectId}")
    @Operation(summary = "Create board", description = "Create a new board for a project")
    public ResponseEntity<ApiResponse<BoardResponse>> createBoard(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String name = request.get("name");
        if (name == null || name.isBlank()) {
            name = "Project Board";
        }

        BoardResponse board = boardService.createBoardForProject(projectId, name);
        return ResponseEntity.ok(new ApiResponse<>(true, "Board created successfully", board));
    }

    @PreAuthorize("hasAuthority('EDIT_BOARD')")
    @PutMapping("/{boardId}/columns/order")
    @Operation(summary = "Update column order", description = "Reorder columns on a board")
    public ResponseEntity<ApiResponse<BoardResponse>> updateColumnOrder(
            @PathVariable Long boardId,
            @RequestBody List<Long> columnIds) {

        BoardResponse board = boardService.updateColumnOrder(boardId, columnIds);
        return ResponseEntity.ok(new ApiResponse<>(true, "Column order updated", board));
    }

    @PreAuthorize("hasAuthority('CREATE_BOARD')")
    @PostMapping("/{boardId}/columns")
    @Operation(summary = "Add column", description = "Add a new column to a board")
    public ResponseEntity<ApiResponse<BoardColumnResponse>> addColumn(
            @PathVariable Long boardId,
            @RequestBody Map<String, String> request) {

        String name = request.get("name");
        BoardColumnResponse column = boardService.addColumn(boardId, name);
        return ResponseEntity.ok(new ApiResponse<>(true, "Column added successfully", column));
    }

    @PreAuthorize("hasAuthority('EDIT_BOARD')")
    @DeleteMapping("/columns/{columnId}")
    @Operation(summary = "Delete column", description = "Delete a column from a board")
    public ResponseEntity<ApiResponse<Void>> deleteColumn(@PathVariable Long columnId) {
        boardService.deleteColumn(columnId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Column deleted successfully", null));
    }

    @PreAuthorize("hasAuthority('EDIT_BOARD')")
    @PatchMapping("/columns/{columnId}")
    @Operation(summary = "Update column", description = "Update a column's name on a board")
    public ResponseEntity<ApiResponse<BoardColumnResponse>> updateColumn(
            @PathVariable Long columnId,
            @RequestBody Map<String, String> request) {

        String name = request.get("name");
        BoardColumnResponse column = boardService.updateColumn(columnId, name);
        return ResponseEntity.ok(new ApiResponse<>(true, "Column updated successfully", column));
    }
}


