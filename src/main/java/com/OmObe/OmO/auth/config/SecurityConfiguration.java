package com.OmObe.OmO.auth.config;

import com.OmObe.OmO.auth.filter.JwtAuthenticationFilter;
import com.OmObe.OmO.auth.filter.JwtExceptionFilter;
import com.OmObe.OmO.auth.filter.JwtLogoutFilter;
import com.OmObe.OmO.auth.filter.JwtVerificationFilter;
import com.OmObe.OmO.auth.handler.*;
import com.OmObe.OmO.auth.jwt.JwtTokenizer;
import com.OmObe.OmO.auth.jwt.TokenService;
import com.OmObe.OmO.auth.oauth.service.OAuth2MemberService;
import com.OmObe.OmO.auth.utils.MemberAuthorityUtils;
import com.OmObe.OmO.member.repository.MemberRepository;
import com.OmObe.OmO.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtTokenizer jwtTokenizer;
    private final MemberAuthorityUtils authorityUtils;
    private final TokenService tokenService;
    private final OAuth2MemberService oAuth2MemberService;
    private final MemberRepository memberRepository;
    private final RedisTemplate redisTemplate;
    private final RedisService redisService;

    // http 요청에 대한 보안 설정 구성
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().sameOrigin() // 동일 출처로부터 들어오는 request만 페이지 렌더링 허용
                .and()
                .csrf().disable() // csrf 공격에 대한 보호 비활성화
                .cors().configurationSource(corsConfigurationSource()) // cors 설정
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 생성X
                .and()
                .formLogin().disable() // 폼 로그인 방식 비활성화
                .httpBasic().disable() // HTTP 기본 인증 비활성화
                .logout().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new MemberAuthenticationEntryPoint()) // MemberAuthenticationEntryPoint 추가
                .accessDeniedHandler(new MemberAccessDeniedHandler()) // MemberAccessDeniedHandler 추가
                .and()
                .apply(new CustomFilterConfigurer()) // jwt 로그인 인증
                .and()
                .authorizeHttpRequests(authorize -> authorize
                        // 관리자 전용 권한 설정
                        .antMatchers(HttpMethod.GET, "/boardReport").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/commentReport").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/reviewReport").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/myCourseReport").hasRole("ADMIN")
                        .antMatchers(HttpMethod.POST, "/notice/write/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.PATCH, "/notice/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/notice/**").hasRole("ADMIN")

                        // 커뮤니티 권한 설정
                        .antMatchers(HttpMethod.POST, "/board/*").hasAnyRole("ADMIN", "USER") // 게시글 작성 권한 설정
                        .antMatchers(HttpMethod.PATCH, "/board/modification/*").hasAnyRole("ADMIN", "USER") // 게시글 수정 권한 설정
                        .antMatchers(HttpMethod.DELETE, "/board/*").hasAnyRole("ADMIN", "USER") // 게시글 삭제 권한 설정
                        .antMatchers(HttpMethod.PUT, "/board/like/*").hasAnyRole("ADMIN", "USER") // 게시글 좋아요 권한 설정

                        // 나만의 코스 권한 설정
                        .antMatchers(HttpMethod.POST, "/mycourse/new").hasAnyRole("ADMIN", "USER") // 나만의 코스 작성 권한 설정
                        .antMatchers(HttpMethod.PUT, "/mycourse/rebuild").hasAnyRole("ADMIN", "USER") // 나만의 코스 수정 권한 설정
                        .antMatchers(HttpMethod.PATCH, "/mycourse/share/*").hasAnyRole("ADMIN", "USER") // 나만의 코스 공유 권한 설정
                        .antMatchers(HttpMethod.DELETE, "/mycourse/*").hasAnyRole("ADMIN", "USER") // 나만의 코스 삭제 권한 설정
                        .antMatchers(HttpMethod.PUT, "/mycourse/like/*").hasAnyRole("ADMIN", "USER") // 나만의 코스 좋아요 권한 설정

                        // 리뷰 권한 설정
                        .antMatchers(HttpMethod.POST, "/review/write").hasAnyRole("ADMIN", "USER") // 리뷰 작성 권한 설정
                        .antMatchers(HttpMethod.PATCH, "/review/modification").hasAnyRole("ADMIN", "USER") // 리뷰 수정 권한 설정
                        .antMatchers(HttpMethod.DELETE, "/review/*").hasAnyRole("ADMIN", "USER") // 리뷰 수정 권한 설정

                        // 전체 허용
                        .antMatchers(HttpMethod.POST, "/h2/**").permitAll() // todo: 테스트용 db 조회 -> 관리자 권한만 접근하도록 수정할 것
                        .antMatchers(HttpMethod.POST, "/signup").permitAll()
                        .antMatchers(HttpMethod.GET, "/board/**").permitAll()
                        .antMatchers(HttpMethod.POST, "/checkNickname").permitAll()
                        .antMatchers(HttpMethod.GET, "/**").permitAll()
                        .anyRequest().authenticated()
                ).oauth2Login(oauth2 -> oauth2 // oauth2 인증 활성화
                        .successHandler(new OAuth2MemberSuccessHandler(tokenService, oAuth2MemberService, authorityUtils, redisService,jwtTokenizer)));

        return http.build();
    }

    // 패스워드 암호화 기능
    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // CORS 정책 설정
    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        // todo : 로그인 기능 테스트를 위해 임시로 모든 origin 오픈함 -> 수정할 것
//        configuration.setAllowedOrigins(Collections.singletonList("*")); // 모든 origin 을 허용
//        configuration.addAllowedOriginPattern("*"); // 모든 origin 패턴을 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173","https://accounts.google.com",
                "https://kauth.kakao.com", "https://nid.naver.com", "https://www.oneulmohae.co.kr")); // http://localhost:5173, oauth 요청에 대해 http 통신 허용
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PATCH", "DELETE", "OPTIONS", "HEAD", "PUT")); // 허용하는 http 메서드
        configuration.setAllowCredentials(true); // 허용된 origin의 자격증명 허용
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Refresh", "x", "y", "Content-Type", "placeId", "memberId", "LR", "review-id")); // 요청 시 허용 헤더 추가 todo 필요한 헤더만 추가하도록 수정 필요
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Refresh", "x", "y", "Content-Type", "placeId", "memberId", "LR", "review-id")); // 응답 헤더
        configuration.setMaxAge(3600L); // 사전 검증(preflight) 요청의 캐시(max-age) 시간 설정

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // JwtAuthenticationFilter 등록
    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            JwtExceptionFilter jwtExceptionFilter = new JwtExceptionFilter();

            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils, redisTemplate, memberRepository);

            JwtLogoutFilter jwtLogoutFilter = new JwtLogoutFilter(jwtTokenizer, redisService, tokenService);

            builder
                    .addFilterBefore(jwtExceptionFilter, OAuth2LoginAuthenticationFilter.class) // JwtExceptionFilter 추가
                    .addFilterAfter(jwtVerificationFilter, OAuth2LoginAuthenticationFilter.class) // OAuth2LoginAuthenticationFilter 이후 JwtVerificationFilter 추가
                    .addFilterAfter(jwtLogoutFilter, JwtVerificationFilter.class); // JwtVerificationFilter 이후 jwtLogoutFilter 추가

        }
    }
}
