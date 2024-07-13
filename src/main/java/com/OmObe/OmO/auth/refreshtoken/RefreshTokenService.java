package com.OmObe.OmO.auth.refreshtoken;

import com.OmObe.OmO.auth.jwt.JwtTokenizer;
import com.OmObe.OmO.auth.jwt.TokenService;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.repository.MemberRepository;
import com.OmObe.OmO.redis.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisService redisService;
    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;
    private final MemberRepository memberRepository;

    /**
     * <리프레시 토큰을 이용한 액세스 토큰 재발급>
     * 리프레시 토큰을 통해 새로운 액세스 토큰과 새로운 리프레시 토큰을 발급받아 새로운 리프레시 토큰으로 다음 액세스 토큰 재발급 가능(Refresh Token Rotation)
     * 1. 액세스 토큰 재발급 요청에 담긴 리프레시 토큰을 통해 회원의 이메일 추출
     * 2. 추출한 이메일의 회원의 리프레시 토큰이 redis에 저장되어 있는지 확인 & 요청 시 들어오는 refreshToken이 redis에 저장된 리프레시 토큰과 일치하는지 확인
     *  2-1. 저장되어 있으면 3번 과정 진행
     *  2-2. redis에 없으면 예외 처리 후 종료
     * 3. 새로운 액세스 토큰 발급 후 Map에 key : "accessToken, value : accessToken으로 저장
     * 4. 기존에 저장되어 있던 리프레시 토큰은 redis에서 제거
     * 5. 새로운 리프레시 토큰 발급 후 Map에 key : "refreshToken", value : refreshToken으로 저장
     */
    public Map<String, String> reissueToken(String refreshToken) {
        // 1. 액세스 토큰 재발급 요청에 담긴 리프레시 토큰을 통해 회원의 이메일 추출
        Jws<Claims> claims = jwtTokenizer.getClaims(refreshToken, jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey()));
        String email = claims.getBody().getSubject();
        Map<String, String> reissuedTokens = new HashMap<>();

        Optional<Member> optionalMember = memberRepository.findByEmail(email);

        String refreshTokenInRedis = (String) redisTemplate.opsForValue().get(email); // 현재 redis에 저장된 리프레시 토큰

        // 2. 추출한 이메일의 회원의 리프레시 토큰이 redis에 저장되어 있는지 확인 & 요청 시 들어오는 refreshToken이 redis에 저장된 리프레시 토큰과 일치하는지 확인
        if (Boolean.TRUE.equals(redisTemplate.hasKey(email)) && optionalMember.isPresent() && (refreshToken.equals(refreshTokenInRedis))) {
            // 2-1. 저장되어 있으면 3번 과정 진행
            Member member = optionalMember.get();

            // 3. 새로운 액세스 토큰 발급 후 Map에 key : "accessToken, value : accessToken으로 저장
            String accessToken = tokenService.delegateAccessToken(member);
            reissuedTokens.put("accessToken", accessToken);

            // 4. 기존에 저장되어 있던 리프레시 토큰은 redis에서 제거
            redisTemplate.delete(email);

            // 5. 새로운 리프레시 토큰 발급 후 Map에 key : "refreshToken", value : refreshToken으로 저장
            String reissuedRefreshToken = tokenService.delegateRefreshToken(member);
            reissuedTokens.put("refreshToken", reissuedRefreshToken);

            // 6. 새로운 리프레시 토큰을 redis에 key : email, value : refreshToken 형식으로 저장
            Long remainExpiration = tokenService.calculateExpiration(claims); // 리프레시 토큰 만료 시간
            redisService.setRefreshToken(email, reissuedRefreshToken, remainExpiration);

            return reissuedTokens;

            // 2-2. redis에 없으면 예외 처리 후 종료
        } else throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
    }

}
