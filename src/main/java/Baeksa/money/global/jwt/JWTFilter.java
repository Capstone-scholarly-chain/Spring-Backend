package Baeksa.money.global.jwt;

import Baeksa.money.domain.Entity.MemberEntity;
import Baeksa.money.domain.enums.Role;
import Baeksa.money.global.redis.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

//        String accessToken = extractTokenFromCookies(request, "access");
        String accessToken = refreshTokenService.getCookieValue(request.getCookies(), "access");
        
        //토큰이 없다면 다음 필터로 넘김
        if (accessToken == null) {

            System.out.println("accessToken null");
            filterChain.doFilter(request, response);

            return;            //조건이 해당되면 메소드 종료 (필수)
        }

        //토큰 만료 여부 확인하고 만료시엔 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e){

            //responseBody
            PrintWriter writer = response.getWriter();
            writer.print("accessToken expired");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //토큰이 access인지 확인 (발급 시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);

        if(!category.equals("access")){

            //responseBody
            PrintWriter writer = response.getWriter();
            writer.print("invalid accessToken");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String username = jwtUtil.getUsername(accessToken);
        Long studentId = jwtUtil.getStudentId(accessToken);
        String role = jwtUtil.getRole(accessToken);

        MemberEntity member = MemberEntity.fromToken(username, studentId, Role.valueOf(role));
        CustomUserDetails customUserDetails = new CustomUserDetails(member);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

//    private String extractTokenFromCookies(HttpServletRequest request, String access) {
//        if (request.getCookies() == null) return null;
//        return Arrays.stream(request.getCookies())
//                .filter(cookie -> cookie.getName().equals(access))
//                .map(cookie -> cookie.getValue())
//                .findFirst()
//                .orElse(null);
//    }

}
