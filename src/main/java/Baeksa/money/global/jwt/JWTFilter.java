package Baeksa.money.global.jwt;

import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.enums.Role;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
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
//클로드 코드
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {

    String requestURI = request.getRequestURI();

    // 토큰 검사 예외 처리: reissue API는 accessToken 만료 전제이므로 필터 제외
    if (requestURI.equals("/api/auth/reissue") ||
            requestURI.equals("/signup") ||
            requestURI.equals("/login")) {
        filterChain.doFilter(request, response);
        return;
    }

    try {
        // 헤더에서 토큰 추출
        String accessToken = extractAccessFromHeader(request);
        log.info("1. accessToken 추출: {}", accessToken);

        // 토큰이 null이 아닌 경우에만 진행
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            try {
                // validateJwt를 통해 토큰 검증 및 Claims 가져오기
                Claims claims = jwtUtil.validateJwt(accessToken);
                log.info("2. 토큰 검증 성공");

                // Claims에서 직접 studentId 가져오기
                String studentId = claims.get("studentId", String.class);

                if (studentId != null) {
                    log.info("3. studentId 추출: {}", studentId);
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(studentId);
                    log.info("4. userDetails 로드됨");

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails,
                                    null, userDetails.getAuthorities());

                    // SecurityContextHolder에 인증 정보 넣기
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("5. 인증 정보 설정 완료");
                } else {
                    log.warn("토큰에 studentId가 없습니다.");
                }
            } catch (RuntimeException e) {
                // validateJwt에서 발생한 예외 처리
                log.warn("토큰 검증 실패: {}", e.getMessage());
            }
        } else {
            log.info("accessToken이 없습니다.");
        }

        filterChain.doFilter(request, response); // 다음 필터로 진행
    } catch (Exception e) {
        log.error("필터 처리 중 예외 발생: {}", e.getMessage());
        filterChain.doFilter(request, response);
    }
}
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        String requestURI = request.getRequestURI();
//
//        // 토큰 검사 예외 처리: reissue API는 accessToken 만료 전제이므로 필터 제외
//        if (requestURI.equals("/api/auth/reissue")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            // 헤더에서 토큰 추출
//            String accessToken = extractAccessFromHeader(request);
////            String accessToken = getCookieValue(request.getCookies(), "access_token");
//            log.info("1");
//
//            // 토큰 검증
//            if (accessToken != null && jwtUtil.isValid(accessToken)) {
//                log.info("2");
//                log.info("accessToken: {}", accessToken);
//
//                // 토큰에서 사용자 정보 가져오기
//                String studentId = jwtUtil.getStudentId(accessToken);
//                log.info("3");
//                UserDetails userDetails = customUserDetailsService.loadUserByUsername(studentId);
//                log.info("4");
//                UsernamePasswordAuthenticationToken authentication =
//                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                log.info("5");
//                // SecurityContextHolder에 인증 정보 넣기
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                log.info("JWTFilter activated. accessToken: {}", accessToken);
//                log.info("studentId: {}", studentId);
//                log.info("authentication: {}", authentication);
//            }
//            log.info("6");
//            filterChain.doFilter(request, response); // 다음 필터로 진행
//            log.info("7");
//        } catch (Exception e) {
//            log.error("토큰 검증 실패: {}", e.getMessage());
//            filterChain.doFilter(request, response);
//        }
//    }
//
//    private String getCookieValue(Cookie[] cookies, String key) {
//        if (cookies == null) return null;
//        for (Cookie cookie : cookies) {
//            if (cookie.getName().equals(key)) return cookie.getValue();
//        }
//        return null;
//    }
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
        log.info("authHeader1: {}", authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.info("authHeader2: {}", authHeader);
            return authHeader.substring(7); // "Bearer " 이후의 실제 토큰 값
        }
        log.info("authHeader3: {}", authHeader);
        return null;
    }
}
