package com.see.realview.search.service;

import com.see.realview._core.exception.ExceptionStatus;
import com.see.realview._core.exception.client.BadRequestException;
import com.see.realview._core.exception.server.ServerException;
import com.see.realview.search.dto.request.KeywordSearchRequest;
import com.see.realview.search.dto.response.NaverSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class NaverSearcher {

    private final WebClient naverWebClient;

    public static int SEARCH_COUNT = 10;


    public NaverSearcher(@Autowired @Qualifier("naverWebClient") WebClient naverWebClient) {
        this.naverWebClient = naverWebClient;
    }

    @Async
    public CompletableFuture<NaverSearchResponse> search(KeywordSearchRequest request) {
        if (request.keyword().isEmpty() || request.cursor() == null) {
            log.error("keyword 또는 cursor 누락됨 | " + request.keyword() + " | " + request.cursor());
            throw new BadRequestException(ExceptionStatus.KEYWORD_IS_EMPTY);
        }

        return CompletableFuture.completedFuture(getSearchResponse(request)
                .orElseThrow(() -> new ServerException(ExceptionStatus.NAVER_SEARCH_ERROR)));
    }

    private Optional<NaverSearchResponse> getSearchResponse(KeywordSearchRequest request) {
        log.debug("키워드 검색 요청 | " + request.keyword() + " | " + request.cursor());
        return naverWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("query", request.keyword())
                        .queryParam("display", SEARCH_COUNT)
                        .queryParam("start", request.cursor())
                        .build())
                .retrieve()
                .bodyToFlux(NaverSearchResponse.class)
                .toStream()
                .findFirst();
    }
}
