package com.see.realview._core.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Component
@Slf4j
public class ImageDownloader {

    private final WebClient imageWebClient;


    public ImageDownloader(@Autowired @Qualifier("imageWebClient") WebClient imageWebClient) {
        this.imageWebClient = imageWebClient;
    }

    public String getEncodedImageFromURL(String url) {
        log.debug("이미지 다운로드 시작 | " + url);
        Mono<byte[]> imageMono = downloadImage(url);
        byte[] imageBytes = imageMono.block();

        if (imageBytes == null || imageBytes.length == 0) {
            log.error("이미지 다운로드 실패 | " + url);
            return "";
        }

        log.debug("이미지 다운로드 완료 | " + url);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private static String getEncodedURL(String url) {
        String[] parts = Arrays.stream(url.split("/"))
                .map(s -> s.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*") ? URLEncoder.encode(s, StandardCharsets.UTF_8) : s)
                .toArray(String[]::new);

        return String.join("/", parts).replace("w80_blur", "w966").replace("http://", "https://");
    }

    private Mono<byte[]> downloadImage(String url) {
        if (url == null) {
            return null;
        }

        String encodedURL = getEncodedURL(url);

        return imageWebClient
                .get()
                .uri(encodedURL)
                .retrieve()
                .bodyToMono(byte[].class);
    }
}
