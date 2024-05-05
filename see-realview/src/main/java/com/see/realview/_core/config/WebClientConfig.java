package com.see.realview._core.config;

import com.see.realview._core.exception.ExceptionStatus;
import com.see.realview._core.exception.server.ServerException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
public class WebClientConfig {

    @Value("${api.search.naver.id}")
    private String CLIENT_ID;

    @Value("${api.search.naver.secret}")
    private String CLIENT_SECRET;

    @Value("${api.search.naver.url}")
    private String REQUEST_URL;

    private final static String GOOGLE_VISION_API_URL = "https://vision.googleapis.com/v1/images:annotate";
    private final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";


    @Bean(name = "naverWebClient")
    public WebClient naverWebClient() {
        return WebClient.builder()
                .baseUrl(REQUEST_URL)
                .defaultHeader("X-Naver-Client-Id", CLIENT_ID)
                .defaultHeader("X-Naver-Client-Secret", CLIENT_SECRET)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                        .build())
                .build();
    }

    @Bean(name = "googleWebClient")
    public WebClient googleWebClient() {
        return WebClient.builder()
                .baseUrl(GOOGLE_VISION_API_URL)
                .defaultHeader("Content-Type", "application/json; charset=utf-8")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                        .build())
                .build();
    }

    @Bean(name = "imageWebClient")
    public WebClient imageWebClient() {
        try {
            SslContext context = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            HttpClient httpClient = HttpClient.create().secure(provider -> provider.sslContext(context));

            return WebClient.builder()
                    .defaultHeader("User-Agent", USER_AGENT)
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .exchangeStrategies(ExchangeStrategies.builder()
                            .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                            .build())
                    .build();
        } catch (SSLException | WebClientResponseException exception) {
            throw new ServerException(ExceptionStatus.IMAGE_PARSING_ERROR);
        }
    }

    @Bean(name = "databaseWebClient")
    public WebClient databaseWebClient() {
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                        .build())
                .build();
    }
}
