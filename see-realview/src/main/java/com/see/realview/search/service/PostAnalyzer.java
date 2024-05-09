package com.see.realview.search.service;

import com.see.realview.google.service.GoogleVisionAPI;
import com.see.realview.image.dto.CachedImage;
import com.see.realview.image.dto.ImageData;
import com.see.realview.image.entity.Image;
import com.see.realview.image.service.ImageService;
import com.see.realview.search.dto.request.AnalyzeRequest;
import com.see.realview.search.dto.request.ImageParseRequest;
import com.see.realview.search.dto.response.AnalyzeResponse;
import com.see.realview.search.dto.response.NaverSearchResponse;
import com.see.realview.search.dto.response.PostDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PostAnalyzer {

    private final RequestConverter requestConverter;

    private final HtmlParser htmlParser;

    private final TextAnalyzer textAnalyzer;

    private final GoogleVisionAPI googleVisionAPI;

    private final ImageService imageService;


    public PostAnalyzer(@Autowired RequestConverter requestConverter,
                        @Autowired HtmlParser htmlParser,
                        @Autowired TextAnalyzer textAnalyzer,
                        @Autowired GoogleVisionAPI googleVisionAPI,
                        @Autowired ImageService imageService) {
        this.requestConverter = requestConverter;
        this.htmlParser = htmlParser;
        this.textAnalyzer = textAnalyzer;
        this.googleVisionAPI = googleVisionAPI;
        this.imageService = imageService;
    }

    @Async
    public CompletableFuture<AnalyzeResponse> analyze(NaverSearchResponse searchResponse) {
        if (Objects.equals(searchResponse.total(), searchResponse.start())) {
            return CompletableFuture.completedFuture(null);
        }
        List<AnalyzeRequest> analyzeRequests = requestConverter.createPostAnalyzeRequest(searchResponse);
        Map<String, Boolean> result = new HashMap<>();
        Map<String, List<String>> imageMap = new HashMap<>();

        analyzeRequests.forEach(analyzeRequest -> {
            result.put(analyzeRequest.link(), false);
        });

        List<ImageParseRequest> imageParseRequests = new ArrayList<>();

        log.debug("포스트 분석 시작");
        List<CompletableFuture<Void>> futures = analyzeRequests
                .stream()
                .map(request -> CompletableFuture.runAsync(() -> analyze(result, imageMap, imageParseRequests, request)))
                .toList();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        log.debug("포스트 분석 완료. Vision API 요청 작업 시작");
        List<String> visionResponse = googleVisionAPI.call(imageParseRequests);

        log.debug("Vision API 작업 완료. 파싱 결과 병합 시작");
        mergeVisionAPIResults(result, imageParseRequests, visionResponse);

        log.debug("클라이언트 응답 생성");
        List<PostDTO> responses = createPostResponses(searchResponse, result, imageMap);

        log.debug("응답 완료");
        Long cursor = searchResponse.start() + responses.size();
        return CompletableFuture.completedFuture(new AnalyzeResponse(cursor, responses));
    }


    private void analyze(Map<String, Boolean> result, Map<String, List<String>> imageMap, List<ImageParseRequest> imageParseRequests, AnalyzeRequest request) {
        Optional<Elements> elements = htmlParser.parse(request);
        if (elements.isEmpty()) {
            return;
        }

        Elements components = elements.get();
        Elements images = components.select("img");
        String text = components.text();
        Boolean advertisement;

        if (images.size() == 0) {
            return;
        }

        log.debug("포스트 이미지 데이터 저장 | " + request.link());
        List<String> imageUrls = getPostImageData(images);
        imageMap.put(request.link(), imageUrls);

        advertisement = imageUrls.stream().anyMatch(imageService::isWellKnownURL);
        if (advertisement) {
            log.debug("이미지에서 광고 확인됨 | " + request.link());
            result.put(request.link(), true);
            return;
        }

        advertisement = textAnalyzer.analyzePostText(text);
        if (advertisement) {
            log.debug("텍스트에서 광고 확인됨 | " + request.link());
            result.put(request.link(), true);
            return;
        }

        Element image = findLastAnalyzableImage(images);
        String url = image.attr("src");
        if (url.equals("")) {
            log.debug("이미지 URL 조회 실패 | " + request.link());
            result.put(request.link(), false);
            return;
        }

        String rawURL = url.replaceAll("\\?.*$", "");
        Optional<CachedImage> cachedImage = imageService.isAlreadyParsedImage(rawURL);
        if (cachedImage.isPresent()) {
            ImageData cachedData = cachedImage.get().data();
            log.debug("이미지 캐싱 정보 확인됨 | " + request.link() + " | " + cachedData.advertisement());
            result.put(request.link(), cachedData.advertisement());
            return;
        }

        log.debug("Vision API 요청 생성 | " + request.link());
        imageParseRequests.add(
                new ImageParseRequest(request.link(), url)
        );
    }

    private Element findLastAnalyzableImage(Elements images) {
        return images
                .stream()
                .filter(image -> isValidAnalyzableImage(image.attr("src")))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private List<String> getPostImageData(Elements images) {
        List<String> imageUrls = new ArrayList<>();
        images.forEach(img -> {
            String imageUrl = img.attr("src").replace("w80_blur", "w966");
            if (isValidPostImage(imageUrl)) {
                imageUrls.add(imageUrl);
            }
        });
        return imageUrls;
    }

    private void mergeVisionAPIResults(Map<String, Boolean> result, List<ImageParseRequest> imageParseRequests, List<String> visionResponse) {
        Queue<String> visionResponseQueue = new LinkedList<>(visionResponse);
        List<Image> images = new ArrayList<>();

        imageParseRequests.forEach(request -> {
            String text = visionResponseQueue.poll();
            boolean advertisement = textAnalyzer.analyzeImageText(text);
            log.debug("Vision API 응답 결과\n" +
                    "Post Link: " + request.postLink() + "\n" +
                    "Image Link: " + request.imageLink() + "\n" +
                    "Text: " + text + "\n" +
                    "advertisement: " + advertisement);

            result.put(request.postLink(), advertisement);

            String url = request.imageLink();
            String rawURL = url.replaceAll("\\?.*$", "");
            images.add(Image.of(rawURL, advertisement));
        });

        log.debug("병합 완료. Vision API 결과 저장 요청");
        imageService.saveAll(images);
        log.debug("Vision API 결과 저장 완료");
    }

    private static List<PostDTO> createPostResponses(NaverSearchResponse searchResponse, Map<String, Boolean> result, Map<String, List<String>> imageMap) {
        return searchResponse.items()
                .stream()
                .map(naverSearchItem -> {
                    String link = naverSearchItem.link();
                    Boolean advertisement = result.get(link);
                    List<String> images = imageMap.get(link);
                    return PostDTO.of(naverSearchItem, advertisement, 0L, images);
                })
                .toList();
    }

    private Boolean isValidPostImage(String url) {
        return !url.equals("")
                && !url.contains("storep-phinf.pstatic.net")
                && isValidAnalyzableImage(url);
    }

    private Boolean isValidAnalyzableImage(String url) {
        return !url.contains("static.map")
                && !url.contains("dthumb-phinf.pstatic.net")
                && !url.contains(".gif")
                && !url.contains("maps.googleapis.com/maps/");
    }
}
