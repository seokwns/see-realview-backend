package com.see.realview.search.dto;

import com.see.realview.search.entity.SearchHistory;

import java.time.LocalDateTime;

public record SearchHistoryDto(
        String keyword,
        LocalDateTime time
) {
    public static SearchHistoryDto of(SearchHistory history) {
        return new SearchHistoryDto(
                history.getId().getKeyword(),
                history.getId().getTime()
        );
    }
}
