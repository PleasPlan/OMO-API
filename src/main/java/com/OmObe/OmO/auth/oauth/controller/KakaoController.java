package com.OmObe.OmO.auth.oauth.controller;

import com.OmObe.OmO.auth.handler.OAuth2MemberSuccessHandler;
import com.OmObe.OmO.auth.jwt.JwtTokenizer;
import com.OmObe.OmO.auth.jwt.TokenService;
import com.OmObe.OmO.auth.oauth.dto.KakaoProfile;
import com.OmObe.OmO.auth.oauth.dto.OAuthToken;
import com.OmObe.OmO.auth.oauth.service.KakaoOAuthService;
import com.OmObe.OmO.auth.oauth.service.OAuth2MemberService;
import com.OmObe.OmO.auth.utils.MemberAuthorityUtils;
import com.OmObe.OmO.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoController {
    private final KakaoOAuthService kakaoOAuthService;
    private final MemberAuthorityUtils authorityUtils;
    private final OAuth2MemberService oAuth2MemberService;
    private final TokenService tokenService;
    private final RedisService redisService;
    private final JwtTokenizer jwtTokenizer;

    @GetMapping("/auth/kakao/callback")
    public ResponseEntity kakaoCallback(@RequestParam("code") String code, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{

        /*
        block()을 사용하면 OAuth 인증 로직에 사용된 WebClient의 non-blocking 이점을 얻기 어렵지만, 사용자 인증로직(OAuth2MemberSuccessHandler, OAuth2AuhenticationToken)이
        정상적으로 동작하는지 안정성 검증이 되지 않았고, WebClient의 이점을 모두 살리려면 나머지 로직의 수정이 필요할 수 있음.
        oauth 토큰 획득과 토큰을 통한 사용자 정보 획득을 block() 처리하여 기존 RestTemplate을 사용했을 때의 로직 처리 흐름을 그대로 가져감.
        단순히 deprecated된 RestTemplate을 대체하기 위한 목적으로 WebClient를 적용.
        */
        OAuthToken oAuthToken = kakaoOAuthService.tokenRequest(code).block(); // 토큰 획득
        KakaoProfile kakaoProfile = kakaoOAuthService.userInfoRequest(oAuthToken).block(); // 사용자 정보 획득

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", kakaoProfile.getId());
        attributes.put("email", kakaoProfile.getKakao_account().getEmail());
        attributes.put("profileImage", kakaoProfile.getProperties().getProfile_image());

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_MEMBER")),
                attributes,
                "id"
        );

        Authentication authentication = new OAuth2AuthenticationToken(oAuth2User, Collections.emptyList(), "kakao");

        AuthenticationSuccessHandler successHandler = new OAuth2MemberSuccessHandler(tokenService, oAuth2MemberService, authorityUtils, redisService, jwtTokenizer);
        successHandler.onAuthenticationSuccess(request, response, authentication);

        return new ResponseEntity(HttpStatus.OK);

    }
}
