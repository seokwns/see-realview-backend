package com.see.realview.search.service;

import com.see.realview.search.dto.SearchHistoryDto;
import com.see.realview.user.entity.UserAccount;

import java.util.List;

public interface SearchHistoryService {

    void save(UserAccount userAccount, String keyword, Long cursor);

    List<SearchHistoryDto> findLatestHistories(UserAccount userAccount);
}
