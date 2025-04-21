package org.example.expert.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {
//Jwt 토큰 생성과 검증하는 유틸 클래스
    private static final String BEARER_PREFIX = "Bearer "; //토큰 앞에 붙는 접두사?
    private static final long TOKEN_TIME = 60 * 60 * 1000L; // 60분, 토큰 유효시간 설정

    @Value("${jwt.secret.key}")
    private String secretKey; //application.properties 에서 가져온 비밀키
    private Key key; // HMAC SHA 암호화 키
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256; // 서명 알고리즘

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
        // secretKey를 디코딩해서 실제 Key 객체로 초기화 (앱 시작할 때 자동 실행
    }
    // 주어진 정보로 JWT 토큰을 생성하는 메서드
    public String createToken(Long userId, String email, UserRole userRole) {
        Date date = new Date();


        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .claim("email", email)
                        .claim("userRole", userRole)
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    // 접두사를 제거하고 실제 JWT만 추출
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new ServerException("Not Found Token");
    }

    // JWT 토큰을 복호화해서 안에 담긴 데이터. Claims를 반환
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
