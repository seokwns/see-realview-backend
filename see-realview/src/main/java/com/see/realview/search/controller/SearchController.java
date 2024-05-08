package com.see.realview.search.controller;

import com.see.realview._core.response.Response;
import com.see.realview._core.security.CustomUserDetails;
import com.see.realview.search.dto.request.KeywordSearchRequest;
import com.see.realview.search.dto.response.AnalyzeResponse;
import com.see.realview.search.dto.response.NaverSearchResponse;
import com.see.realview.search.service.NaverSearcher;
import com.see.realview.search.service.PostAnalyzer;
import com.see.realview.search.service.SearchHistoryService;
import com.see.realview.user.entity.UserAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/search")
@Slf4j
public class SearchController {

    private final NaverSearcher naverSearcher;

    private final PostAnalyzer postAnalyzer;

    private final SearchHistoryService searchHistoryService;


    public SearchController(@Autowired NaverSearcher naverSearcher,
                            @Autowired PostAnalyzer postAnalyzer,
                            @Autowired SearchHistoryService searchHistoryService) {
        this.naverSearcher = naverSearcher;
        this.postAnalyzer = postAnalyzer;
        this.searchHistoryService = searchHistoryService;
    }

    @GetMapping("")
    public CompletableFuture<ResponseEntity<?>> searchKeyword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1")
            Long cursor)
    {
        log.debug("+---------------------------------------------+");
        log.debug("|               새로운 검색 요청               |");
        log.debug("+---------------------------------------------+");

        KeywordSearchRequest request = new KeywordSearchRequest(keyword, cursor);
        CompletableFuture<NaverSearchResponse> searchFuture = naverSearcher.search(request);
        CompletableFuture<AnalyzeResponse> response = searchFuture.thenCompose(postAnalyzer::analyze);
        UserAccount userAccount = userDetails.userAccount();
        searchHistoryService.save(userAccount, keyword);

        return response.thenApply(result -> ResponseEntity.ok().body(Response.success(result)));
    }
}
