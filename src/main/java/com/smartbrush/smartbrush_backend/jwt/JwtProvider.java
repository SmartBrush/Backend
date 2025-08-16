package com.smartbrush.smartbrush_backend.jwt;

import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import com.smartbrush.smartbrush_backend.service.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.security.Key;

@Component
public class JwtProvider {

    private final SecretKey key = Keys.hmacShaKeyFor("mysmartbrushjwtsecretkey1234567890".getBytes());
    private final long tokenValidity = 1000L * 60 * 60; // 1시간
//    private final long tokenValidity = 1000L * 60 * 1; // 1분 실험

    private final AuthRepository authRepository;

    public JwtProvider(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public String createToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidity);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

//    public Authentication getAuthentication(String token) {
//        String email = getEmail(token);
//
//        // 주입받은 authRepository로 사용자 조회
//        AuthEntity user = authRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("사용자 없음"));
//
//        UserDetailsImpl userDetails = new UserDetailsImpl(user);
//
//        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
//    }

    public String createRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 14)) // 14일
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        String email = getEmail(token);

        // 사용자 없으면 새로 생성
        AuthEntity user = authRepository.findByEmail(email)
                .orElseGet(() -> {
                    AuthEntity newUser = new AuthEntity();
                    newUser.setEmail(email);
                    newUser.setPassword(""); // 소셜 로그인 또는 외부 로그인인 경우
                    newUser.setNickname("사용자_" + System.currentTimeMillis()); // 기본 닉네임 설정
                    return authRepository.save(newUser);
                });

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getEmail(String token) {
        return Jwts.parser().setSigningKey(key)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
