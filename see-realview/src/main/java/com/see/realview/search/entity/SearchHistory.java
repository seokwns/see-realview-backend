package com.see.realview.search.entity;

import com.see.realview.user.entity.UserAccount;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history_tb")
@NoArgsConstructor
@Getter
public class SearchHistory {
    @EmbeddedId
    private SearchHistoryId id;

    public SearchHistory(SearchHistoryId id) {
        this.id = id;
    }

    @Builder
    public SearchHistory(UserAccount userAccount, String keyword, LocalDateTime time) {
        this.id = new SearchHistoryId(
                userAccount,
                keyword,
                time
        );
    }

    public static SearchHistory of(UserAccount userAccount, String keyword) {
        SearchHistoryId historyId = SearchHistoryId.of(userAccount, keyword);
        return new SearchHistory(historyId);
    }
}
