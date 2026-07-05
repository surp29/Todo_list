package com.example.todolist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic wrapper describing a paginated result set.
 *
 * @param <T> the type of items contained in the page
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponseDTO<T> {

    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean isFirst;
    private boolean isLast;

    /**
     * Builds a {@link PageResponseDTO} from a Spring Data {@link Page}, mapping its content
     * with the supplied converter function.
     */
    public static <S, T> PageResponseDTO<T> from(Page<S> page, List<T> mappedContent) {
        return PageResponseDTO.<T>builder()
                .content(mappedContent)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
