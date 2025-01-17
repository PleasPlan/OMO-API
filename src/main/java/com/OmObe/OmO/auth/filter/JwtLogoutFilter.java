package com.OmObe.OmO.auth.filter;

import com.OmObe.OmO.auth.jwt.JwtTokenizer;
import com.OmObe.OmO.auth.jwt.TokenService;
import com.OmObe.OmO.auth.utils.ErrorResponder;
import com.OmObe.OmO.redis.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public class JwtLogoutFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final RedisService redisService;
    private final TokenService tokenService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        String refreshToken = request.getHeader("Refresh");

        // 요청 url이 POST /logout 이 아닌 경우 필터를 적용하지 않음 || 리프레시 토큰이 없으면 필터 적용하지 않음
        return !request.getMethod().equals("POST")
                || !uri.equals("/logout")
                || !StringUtils.hasText(refreshToken);
    }

    /**
     * <로그아웃>
     * 1. request를 통해 accessToken, refreshToken 추출
     * 2. 남은 유효시간 계산
     * 3. redis에 저장된 refreshToken 삭제
     * 4. redis에 accessToken 저장(로그아웃 된 토큰)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. request를 통해 accessToken, refreshToken 추출
            String accessToken = extractAccessToken(request, response);
            String refreshToken = extractRefreshToken(request, response);

            // 2. 남은 유효시간 계산
            Jws<Claims> claims = jwtTokenizer.getClaims(accessToken, jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey()));
            Long remainExpiration = tokenService.calculateExpiration(claims);

            // 3. redis에 저장된 refreshToken(key : email, value : refreshToken) 삭제
            String email = claims.getBody().getSubject();
            redisService.deleteRefreshToken(email);

            // 4. redis에 accessToken 저장(로그아웃 된 토큰)
            redisService.setLogoutAccessToken(accessToken, remainExpiration);
            log.info("로그아웃 성공, 남은 만료 시간 : {}", remainExpiration);

            response.setStatus(HttpStatus.OK.value());
            response.setCharacterEncoding("utf-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        } catch (Exception e) {
            ErrorResponder.sendErrorResponse(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // access token 추출 메서드
    private String extractAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String accessToken = request.getHeader("Authorization");
        // access token이 없는 경우 예외처리
        if (!StringUtils.hasText(accessToken) || !accessToken.startsWith("Bearer ")) {
            ErrorResponder.sendErrorResponse(response, HttpStatus.BAD_REQUEST);
        }
        return accessToken.replace("Bearer ", "");
    }

    // refresh token 추출 메서드
    private String extractRefreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refreshToken = request.getHeader("Refresh");
        if (!StringUtils.hasText(refreshToken)) {
            ErrorResponder.sendErrorResponse(response, HttpStatus.BAD_REQUEST);
        }
        return refreshToken;
    }
}
