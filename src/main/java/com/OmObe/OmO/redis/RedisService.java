package com.OmObe.OmO.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RedisService { // 토큰을 redis에서 관리하는 로직
    private final RedisTemplate redisTemplate;

    // 액세스 토큰을 redis에 저장하는 메서드 (key : accessToken, value : "logout")
    public void setLogoutAccessToken(String accessToken, Long expiration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
        // 토큰 만료시간 계산
        String expirationTime = String.format("%d min %d sec",
                TimeUnit.MILLISECONDS.toMinutes(expiration),
                TimeUnit.MILLISECONDS.toSeconds(expiration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(expiration)));

        log.info("Access Token expiration time : {}", expirationTime);
    }

    // redis에 저장된 accessToken 조회
    public String getAccessToken(String accessToken) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        return valueOperations.get(accessToken).replace("Bearer ", "");
    }

    // 리프레시 토큰을 redis에 저장하는 메서드 (key : email, value : refreshToken)
    public void setRefreshToken(String email, String refreshToken, Long expiration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        // refreshToken의 만료 시간 저장
        valueOperations.set(email, refreshToken, expiration, TimeUnit.MILLISECONDS);

        // 토큰 만료시간 계산
        String expirationTime = String.format("%d min %d sec",
                TimeUnit.MILLISECONDS.toMinutes(expiration),
                TimeUnit.MILLISECONDS.toSeconds(expiration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(expiration)));
        log.info("Refresh Token expiration time : {}", expirationTime);
    }

    // redis에 저장된 refreshToken 조회
    public String getRefreshToken(String email) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(email);
    }

    // redis에 저장된 refreshToken 삭제
    public void deleteRefreshToken(String refreshToken) {
        redisTemplate.delete(refreshToken);
    }
}
