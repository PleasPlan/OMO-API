package com.OmObe.OmO.auth.config;

import com.OmObe.OmO.auth.filter.JwtAuthenticationFilter;
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
                .cors(Customizer.withDefaults()) // cors 설정
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

                        .antMatchers(HttpMethod.GET, "/boardReport").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/commentReport").hasRole("ADMIN")
                        .antMatchers(HttpMethod.POST, "/h2/**").permitAll() // todo: 테스트용 db 조회 -> 관리자 권한만 접근하도록 수정할 것
                        .antMatchers(HttpMethod.GET, "/**").permitAll()
                        .antMatchers(HttpMethod.POST, "/signup").permitAll()
                        .antMatchers(HttpMethod.GET, "/board/**").permitAll()
                        .antMatchers(HttpMethod.POST, "/notice/write/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.PATCH, "/notice/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/notice/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                ).oauth2Login(oauth2 -> oauth2 // oauth2 인증 활성화
                        .successHandler(new OAuth2MemberSuccessHandler(tokenService, oAuth2MemberService, authorityUtils)));

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
        configuration.setAllowedOrigins(Arrays.asList("*")); // 모든 origin에 대해 http 통신 허용
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PATCH", "DELETE")); // 허용하는 http 메서드

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // JwtAuthenticationFilter 등록
    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authentic = builder.getSharedObject(AuthenticationManager.class);

            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authentic, jwtTokenizer, tokenService, redisService);
//            jwtAuthenticationFilter.setFilterProcessesUrl("/login");
            jwtAuthenticationFilter.setAuthenticationSuccessHandler(new MemberAuthenticationSuccessHandler());
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new MemberAuthenticationFailureHandler());

            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils, redisTemplate, memberRepository);

            JwtLogoutFilter jwtLogoutFilter = new JwtLogoutFilter(jwtTokenizer, redisService);

            builder
                    .addFilter(jwtAuthenticationFilter) // spring security filter chain에 JwtAuthenticationFilter 추가
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class) // JwtAuthenticationFilter jwtVerificationFilter 추가
                    .addFilterAfter(jwtVerificationFilter, OAuth2LoginAuthenticationFilter.class) // OAuth2LoginAuthenticationFilter 이후 JwtVerificationFilter 추가
                    .addFilterAfter(jwtLogoutFilter, JwtVerificationFilter.class); // JwtVerificationFilter 이후 jwtLogoutFilter 추가

        }
    }
}
