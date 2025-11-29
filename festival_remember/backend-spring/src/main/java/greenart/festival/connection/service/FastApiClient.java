package greenart.festival.connection.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class FastApiClient {

    private final WebClient webClient;

    public FastApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://127.0.0.1:8000/").build();
    }

    public Mono<String> forwardParameters(String searchKeyword, String regionCode){
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("search_keyword", searchKeyword)
                        .queryParam("region_code", regionCode)
                        .build())
                .retrieve()
                .bodyToMono(String.class);

    }


    public Mono<String> recommendParameters(Long userId, int k, double mbtiWeight) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/hybrid_recommendations/{user_id}")
                        .queryParam("k", k)
                        .queryParam("mbti_weight", mbtiWeight)
                        .build(userId))
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> mbtiParameters(String mbti, int k) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/recommendations/mbti")
                        .queryParam("search_mbti", mbti) // 검색할 MBTI 추가
                        .queryParam("k", k) // 추천 개수 추가
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }
}
