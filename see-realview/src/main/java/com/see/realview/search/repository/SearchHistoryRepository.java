package com.see.realview.search.repository;

import com.see.realview.search.entity.SearchHistory;
import com.see.realview.user.entity.UserAccount;

import java.util.List;

public interface SearchHistoryRepository {

    void save(SearchHistory searchHistory);

    List<SearchHistory> findLatestHistories(UserAccount userAccount);
}
