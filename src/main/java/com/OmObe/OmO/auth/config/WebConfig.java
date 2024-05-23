package com.OmObe.OmO.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .exposedHeaders("Authorization", "Refresh", "x", "y", "Content-Type", "placeId", "memberId", "LR")
                .allowedOrigins("http://localhost:5173", "https://www.oneulmohae.co.kr", "https://accounts.google.com", "https://kauth.kakao.com", "https://nid.naver.com")
                .allowedMethods("GET","POST","PATCH", "DELETE", "OPTIONS", "HEAD", "PUT");
    }
}
