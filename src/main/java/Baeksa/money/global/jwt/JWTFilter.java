package Baeksa.money.global.jwt;

import Baeksa.money.domain.Entity.MemberEntity;
import Baeksa.money.domain.enums.Role;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.ErrorCode;
import Baeksa.money.global.redis.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

// LoginFilter에서 로그인처리와 JWT를 발급하고
// 이 JWTFilter는 OncePerRequestFilter를 구현중
// JWTFilter는 모든 요청에 대해 accessToken을 검증함
@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 로그인 요청은 필터 통과
        if (uri.equals("/login") || uri.equals("/signup")) {
            filterChain.doFilter(request, response);
            return;
        }

//        String accessToken = refreshTokenService.getCookieValue(request.getCookies(), "access");
        String accessToken = refreshTokenService.extractAccessFromHeader(request);

        //토큰이 없다면 다음 필터로 넘기지말고 에러 반환
        if (accessToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"No access token provided\"}");
            return;
        }

        //토큰 만료 여부 확인하고 만료시엔 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            //responseBody
            PrintWriter writer = response.getWriter();
            writer.print("{\"error\": \"Access token expired\"}");
            return;
        }

        //토큰이 access인지 확인 (발급 시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);

        if(!category.equals("access")){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid access token\"}");
            return;
        }

        //블랙리스트에 있는 access인지 검사
        if (refreshTokenService.isBlacklisted(accessToken)){
            throw new CustomException(ErrorCode.BLACKLISTED);
        }

        Long studentId = jwtUtil.getStudentId(accessToken);
        String role = jwtUtil.getRole(accessToken);

        MemberEntity member = MemberEntity.fromToken(studentId, Role.valueOf(role));
        CustomUserDetails customUserDetails = new CustomUserDetails(member);

        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);

        log.info("JWTFilter activated. accessToken: {}", accessToken);
        log.info("studentId: {}", studentId);
        log.info("authToken: {}", authToken);

    }
}
