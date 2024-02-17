package com.OmObe.OmO.auth.refreshtoken;

import com.OmObe.OmO.auth.jwt.JwtTokenizer;
import com.OmObe.OmO.auth.jwt.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenController {
    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    // 리프레시 토큰을 통한 액세스 토큰 재발급
    @GetMapping("/reissueToken")
    public ResponseEntity<HttpStatus> reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = request.getHeader("Refresh");

        String accessToken = "Bearer " + refreshTokenService.reissueToken(refreshToken);
        response.setHeader("Authorization", accessToken);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
