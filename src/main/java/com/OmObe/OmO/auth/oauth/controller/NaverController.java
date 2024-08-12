package com.OmObe.OmO.auth.oauth.controller;

import com.OmObe.OmO.auth.handler.OAuth2MemberSuccessHandler;
import com.OmObe.OmO.auth.jwt.JwtTokenizer;
import com.OmObe.OmO.auth.jwt.TokenService;
import com.OmObe.OmO.auth.oauth.dto.NaverProfile;
import com.OmObe.OmO.auth.oauth.dto.OAuthToken;
import com.OmObe.OmO.auth.oauth.service.NaverOAuthService;
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
public class NaverController {
    private final NaverOAuthService naverOAuthService;
    private final MemberAuthorityUtils authorityUtils;
    private final OAuth2MemberService oAuth2MemberService;
    private final TokenService tokenService;
    private final RedisService redisService;
    private final JwtTokenizer jwtTokenizer;

    @GetMapping("/auth/naver/callback")
    public ResponseEntity naverCallback(@RequestParam("code") String code,
                                        @RequestParam("state") String state,
                                        HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        OAuthToken oAuthToken = naverOAuthService.tokenRequest(code).block(); // 토큰 획득
        log.info("code : {}", code);

        NaverProfile naverProfile = naverOAuthService.userInfoRequest(oAuthToken).block(); // 사용자 정보 획득

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", naverProfile.getResponse().getId());
        attributes.put("email", naverProfile.getResponse().getEmail());
        attributes.put("profileImage", naverProfile.getResponse().getProfile_image());

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "id"
        );

        Authentication authentication = new OAuth2AuthenticationToken(oAuth2User, Collections.emptyList(), "naver");

        AuthenticationSuccessHandler successHandler = new OAuth2MemberSuccessHandler(tokenService, oAuth2MemberService, authorityUtils, redisService, jwtTokenizer);
        successHandler.onAuthenticationSuccess(request, response, authentication);

        return new ResponseEntity(HttpStatus.OK);
    }
}
