package com.OmObe.OmO.auth.oauth.service;

import com.OmObe.OmO.auth.oauth.dto.KakaoProfile;
import com.OmObe.OmO.auth.oauth.dto.OAuthToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class KakaoOAuthService {
    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    public KakaoOAuthService() {
        this.webClient = WebClient.builder()
                .defaultHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .build();
    }

    // 카카오 api 통해 액세스 토큰 요청
    public Mono<OAuthToken> tokenRequest(String code) {
        // kakao api 엔드포인트를 통해 액세스 토큰 요청을 보내고 응답을 받음
        return webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .bodyValue(createTokenRequestBody(code))// 요청 body 설정
                // 응답을 Mono<OAuthToken> 형태로 변환
                .retrieve()
                .bodyToMono(OAuthToken.class);
    }

    // kakao api로 사용자의 정보 요청
    public Mono<KakaoProfile> userInfoRequest(OAuthToken oAuthToken) {
        // kakao api 엔드포인트를 통해 액세스 토큰 요청을 보내고 응답을 받음
        return webClient.post()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + oAuthToken.getAccess_token()) // Http Header 설정
                // 응답을 Mono<KakaoProfile> 형태로 변환
                .retrieve()
                .bodyToMono(KakaoProfile.class);
    }

    // 토큰 요청에 필요한 Body 생성
    private MultiValueMap<String, String> createTokenRequestBody(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", "https://api.oneulmohae.co.kr/auth/kakao/callback");
        body.add("code", code);

        return body;
    }

}
