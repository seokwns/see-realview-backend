package com.see.realview.search.entity;

import com.see.realview.user.entity.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Embeddable
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class SearchHistoryId implements Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    private UserAccount userAccount;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private LocalDateTime time;

    @Builder
    public SearchHistoryId(UserAccount userAccount, String keyword, LocalDateTime time) {
        this.userAccount = userAccount;
        this.keyword = keyword;
        this.time = (time == null ? LocalDateTime.now() : time);
    }

    public static SearchHistoryId of(UserAccount userAccount, String keyword) {
        return new SearchHistoryId(userAccount, keyword, null);
    }
}
