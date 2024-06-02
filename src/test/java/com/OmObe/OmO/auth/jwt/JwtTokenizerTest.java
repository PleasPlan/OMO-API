package com.OmObe.OmO.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.io.Decoders;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class JwtTokenizerTest {
//    private static JwtTokenizer jwtTokenizer;
//    private String secretKey;
//    private String base64EncodedSecretKey;
//
//    @BeforeAll
//    public void init(){ // 초기화 메서드
//        // 테스트에 쓸 secretKey를 Base64 형식으로 인코딩
//        jwtTokenizer = new JwtTokenizer();
//        secretKey = "hong1234123412341234123412341234";
//
//        base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(secretKey);
//    }
//
//    @Test
//    public void encodedBase64SecretKeyTest(){
//        System.out.println(base64EncodedSecretKey);
//
//        MatcherAssert.assertThat(secretKey, is(new String(Decoders.BASE64.decode(base64EncodedSecretKey))));
//    }
//
//    @Test
//    public void generateAccessToken(){ // access token 생성 테스트
//
//        // given
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("memberId", 1L);
//        claims.put("roles", List.of("USER"));
//
//        String subject = "generate access token";
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.MINUTE, 10);
//        Date expiration = calendar.getTime();
//
//        // when
//        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);
//        System.out.println(accessToken);
//
//        // then
//        MatcherAssert.assertThat(accessToken, notNullValue());
//    }
//
//    @Test
//    public void generateRefreshToken() { // refresh token 생성 테스트
//
//        // given
//        String subject = "generate refresh token";
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.HOUR, 10);
//        Date expiration = calendar.getTime();
//
//        // when
//        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey);
//        System.out.println(refreshToken);
//
//        // then
//        MatcherAssert.assertThat(refreshToken, notNullValue());
//    }
//
//    @Test
//    @DisplayName("jwt 검증 테스트")
//    public void verifySignatureTest(){
//        String accessToken = getAccessToken(Calendar.MINUTE, 10);
//        assertDoesNotThrow(() -> jwtTokenizer.verifySignature(accessToken, base64EncodedSecretKey));
//    }
//
//    @Test
//    @DisplayName("jwt 만료 테스트")
//    public void verifyExpirationTest() throws InterruptedException {
//        String accessToken = getAccessToken(Calendar.SECOND, 1);
//        assertDoesNotThrow(() -> jwtTokenizer.verifySignature(accessToken, base64EncodedSecretKey));
//
//        TimeUnit.MILLISECONDS.sleep(1500);
//
//        assertThrows(ExpiredJwtException.class, () -> jwtTokenizer.verifySignature(accessToken, base64EncodedSecretKey));
//    }
//
//    private String getAccessToken(int timeUnit, int timeAmount) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("memberId", 1L);
//        claims.put("roles", List.of("USER"));
//
//        String subject = "accesstoken test";
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(timeUnit, timeAmount);
//        Date expiration = calendar.getTime();
//        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);
//
//        return accessToken;
//    }
//
//}