package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Paginated response wrapper")
public class PaginatedResponse<T> {

    @Schema(description = "List of items for the current page")
    private List<T> content;

    @Schema(example = "0", description = "Current page number (zero-indexed)")
    private int page;

    @Schema(example = "10", description = "Number of items per page")
    private int size;

    @Schema(example = "100", description = "Total number of items across all pages")
    private long totalElements;

    @Schema(example = "10", description = "Total number of pages")
    private int totalPages;

    @Schema(example = "false", description = "Whether this is the last page")
    private boolean last;

    public static <T> PaginatedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        PaginatedResponse<T> response = new PaginatedResponse<>();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages((int) Math.ceil((double) totalElements / size));
        response.setLast(page >= response.getTotalPages() - 1);
        return response;
    }
}



