package com.vinncorp.erp.shared.mapper;

import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import org.springframework.data.domain.Page;

import java.util.function.Function;

public class PaginationMapper {

    public static <T, R> PaginatedResponse<R> toPaginatedResponse(
            Page<T> page,
            Function<T, R> mapper
    ) {
        PaginatedResponse<R> response = new PaginatedResponse<>();

        response.setContent(
                page.getContent()
                        .stream()
                        .map(mapper)
                        .toList()
        );

        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());

        return response;
    }
}


