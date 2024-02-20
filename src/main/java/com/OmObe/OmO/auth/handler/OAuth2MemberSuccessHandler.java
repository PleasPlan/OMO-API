package com.OmObe.OmO.auth.handler;

import com.OmObe.OmO.auth.jwt.JwtTokenizer;
import com.OmObe.OmO.auth.jwt.TokenService;
import com.OmObe.OmO.auth.oauth.service.OAuth2MemberService;
import com.OmObe.OmO.auth.utils.MemberAuthorityUtils;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
// Oauth2인증이 성공적으로 수행되면 호출되는 핸들러
public class OAuth2MemberSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final TokenService tokenService;
    private final OAuth2MemberService oAuth2MemberService;
    private final MemberAuthorityUtils authorityUtils;
    private final RedisService redisService;
    private final JwtTokenizer jwtTokenizer;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        var oAuth2User = (OAuth2User)authentication.getPrincipal(); // authentication 객체에서 OAuth2 사용자 정보를 추출
//        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = String.valueOf(oAuth2User.getAttributes().get("email"));
        List<String> authorities = authorityUtils.createRoles(email);

        Member member = oAuth2MemberService.createOAuth2Member(oAuth2User); // OAuth2 사용자 정보 저장

        redirect(request, response, member); // Oauth2 사용자의 정보를 Member에 맞게 매핑하고 파라미터에 member 추가 예정

    }

    // 토큰 생성 후 리다이렉트
    private void redirect(HttpServletRequest request, HttpServletResponse response, Member member) throws IOException {
        String accessToken = tokenService.delegateAccessToken(member);
        String refreshToken = tokenService.delegateRefreshToken(member);
        // redis에 refreshToken 저장
        saveRefreshTokenInRedis(refreshToken, member);
        // Response Header에 Access Token, Refresh Token을 설정
        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addHeader("Refresh", refreshToken);

        // 최초 로그인한 멤버인지 판별하여 리다이렉트 될 경로 설정
        String path = distinctionPath(member);
        log.info("path : {}", path);

        String uri = createURI(path).toString(); // 리다이렉트할 url
        getRedirectStrategy().sendRedirect(request, response, uri);
    }

    // redis에 로그인 한 사용자의 refresh token이 없으면 해당 토큰을 redis에 저장하는 메서드
    private void saveRefreshTokenInRedis(String refreshToken, Member member) {
        if (redisService.getRefreshToken(refreshToken) == null) {
            redisService.setRefreshToken(member.getEmail(), refreshToken, jwtTokenizer.getRefreshTokenExpirationMinutes());
            log.info("Refresh Token Saved in Redis");
        }
    }

    /*
    <최초 로그인 한 멤버인지 판별하는 메서드>
    - 회원의 권한이 "GUEST"면 최초 로그인한 사용자로 판단하여 회원 추가 정보 입력 기능으로 redirect
    - 회원의 권한이 "GUEST"가 아니면 메인화면으로 redirect
     */
    private String distinctionPath(Member member) {
        log.info("member Role : {}", member.getMemberRole().getRole());
        // 회원의 권한이 "GUEST"면 최초 로그인한 사용자로 판단하여 회원 추가 정보 입력 기능으로 redirect
        if (member.getMemberRole().getRole().equals("ROLE_GUEST")) {
            return "/memberInfo/" + member.getMemberId(); // todo: 프론트엔드 배포 후 추가 정보 입력 화면으로 redirect하도록 변경
        }else { // 회원의 권한이 "GUEST"가 아니면 메인화면으로 redirect
            return "";
        }
    }

    private URI createURI(String path) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

        return UriComponentsBuilder
                .newInstance()
                .scheme("http")
//                .scheme("https")
                .host("localhost") // 애플리케이션 배포 후 변경 예정
//                .host("api.oneulmohae.co.kr") // todo : 프론트엔드 배포 후 변경 예정
                .port(8080)
//                .port(3000)
                .path(path)
//                .queryParams(queryParams)
                .build()
                .toUri();
    }
}