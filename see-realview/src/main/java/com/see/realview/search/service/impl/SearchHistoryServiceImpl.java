package com.see.realview.search.service.impl;

import com.see.realview.search.dto.SearchHistoryDto;
import com.see.realview.search.entity.SearchHistory;
import com.see.realview.search.repository.SearchHistoryRepository;
import com.see.realview.search.service.SearchHistoryService;
import com.see.realview.user.entity.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchHistoryServiceImpl implements SearchHistoryService {
    private final SearchHistoryRepository searchHistoryRepository;


    public SearchHistoryServiceImpl(@Autowired SearchHistoryRepository searchHistoryRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
    }

    @Override
    public void save(UserAccount userAccount, String keyword, Long cursor) {
        if (userAccount == null || cursor != 1) {
            return;
        }

        SearchHistory searchHistory = SearchHistory.of(userAccount, keyword);
        searchHistoryRepository.save(searchHistory);
    }

    @Override
    public List<SearchHistoryDto> findLatestHistories(UserAccount userAccount) {
        List<SearchHistory> histories = searchHistoryRepository.findLatestHistories(userAccount);
        return histories
                .stream()
                .map(SearchHistoryDto::of)
                .toList();
    }
}
