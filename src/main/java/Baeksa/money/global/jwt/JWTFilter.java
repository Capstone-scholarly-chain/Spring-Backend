package Baeksa.money.global.jwt;

import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.enums.Role;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.io.IOException;
import java.io.PrintWriter;

// LoginFilter에서 로그인처리와 JWT를 발급하고
// 이 JWTFilter는 OncePerRequestFilter를 구현중
// JWTFilter는 모든 요청에 대해 accessToken을 검증함
@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 토큰 검사 예외 처리: reissue API는 accessToken 만료 전제이므로 필터 제외
        if (requestURI.equals("/api/auth/reissue")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 헤더에서 토큰 추출
//            String accessToken = extractAccessFromHeader(request);
            String accessToken = getCookieValue(request.getCookies(), "access_token");


            // 토큰 검증
            if (accessToken != null && jwtUtil.isValid(accessToken)) {

                // 토큰에서 사용자 정보 가져오기
                String studentId = jwtUtil.getStudentId(accessToken);

                UserDetails userDetails = customUserDetailsService.loadUserByUsername(studentId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // SecurityContextHolder에 인증 정보 넣기
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JWTFilter activated. accessToken: {}", accessToken);
                log.info("studentId: {}", studentId);
                log.info("authentication: {}", authentication);
            }

            filterChain.doFilter(request, response); // 다음 필터로 진행

        } catch (Exception e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    private String getCookieValue(Cookie[] cookies, String key) {
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(key)) return cookie.getValue();
        }
        return null;
    }
//        String accessToken = refreshTokenService.getCookieValue(request.getCookies(), "access");
//        String accessToken = refreshTokenService.extractAccessFromHeader(request);

//        String studentId = jwtUtil.getStudentId(accessToken);
//        String role = jwtUtil.getRole(accessToken);
//
//        MemberEntity member = MemberEntity.fromToken(studentId, Role.valueOf(role));
//        CustomUserDetails customUserDetails = new CustomUserDetails(member);
//
//        Authentication authToken = new UsernamePasswordAuthenticationToken(
//                customUserDetails, null, customUserDetails.getAuthorities());
//
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//        filterChain.doFilter(request, response);
//
//        log.info("JWTFilter activated. accessToken: {}", accessToken);
//        log.info("studentId: {}", studentId);
//        log.info("authToken: {}", authToken);
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("✅ 최종 인증 객체: " + auth);
//
//    }

    //헤더에서 access 토큰 꺼냄
    public String extractAccessFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // "Bearer " 이후의 실제 토큰 값
        }
        return "access토큰 못꺼냄";
    }
}
