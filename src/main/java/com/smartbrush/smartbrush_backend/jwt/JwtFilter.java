package com.smartbrush.smartbrush_backend.jwt;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.smartbrush.smartbrush_backend.code.ResponseCode;
//import com.smartbrush.smartbrush_backend.dto.response.ResponseDTO;
//import com.smartbrush.smartbrush_backend.entity.AuthEntity;
//import com.smartbrush.smartbrush_backend.repository.AuthRepository;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.JwtException;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.Optional;
//
//@Component
//@RequiredArgsConstructor
//public class JwtFilter extends OncePerRequestFilter {
//
//    private final JwtProvider jwtProvider;
//    private final AuthRepository authRepository;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        // 1. Authorization 헤더에서 토큰 추출
//        String token = jwtProvider.resolveToken(request);
//
//        // 2. 토큰이 유효하다면 SecurityContext에 인증 정보 저장
//        if (token != null && jwtProvider.validateToken(token)) {
//            var authentication = jwtProvider.getAuthentication(token);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            String email = jwtProvider.getEmail(token);
//            Optional<AuthEntity> userOpt = authRepository.findByEmail(email);
//
//            if (userOpt.isPresent()) {
//                AuthEntity user = userOpt.get();
//                request.setAttribute("userId", user.getId());
//                request.setAttribute("author", user.getNickname());
//                request.setAttribute("email", user.getEmail());
//                request.setAttribute("profileImage", ""); // 필요시 사용자 프로필 경로 연결
//
//
//            }
//        }
//
//        // 3. 다음 필터로 진행
//        filterChain.doFilter(request, response);
//    }
//}

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbrush.smartbrush_backend.code.ErrorCode;
import com.smartbrush.smartbrush_backend.code.ResponseCode;
import com.smartbrush.smartbrush_backend.dto.response.ResponseDTO;
import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AuthRepository authRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final List<String> WHITELIST = List.of(
            "/api/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/error"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        return WHITELIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtProvider.resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                jwtProvider.validateToken(token); // 유효하지 않으면 예외 throw

                var authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String email = jwtProvider.getEmail(token);
                Optional<AuthEntity> userOpt = authRepository.findByEmail(email);
                userOpt.ifPresent(user -> {
                    request.setAttribute("userId", user.getId());
                    request.setAttribute("author", user.getNickname());
                    request.setAttribute("email", user.getEmail());
                    request.setAttribute("profileImage", "");
                });

            } catch (ExpiredJwtException ex) {
                writeError(response, ResponseCode.TOKEN_EXPIRED);
                return;
            } catch (JwtException | IllegalArgumentException ex) {
                writeError(response, ResponseCode.INVALID_TOKEN);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, ResponseCode code) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        var body = new ResponseDTO<>(code, null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
