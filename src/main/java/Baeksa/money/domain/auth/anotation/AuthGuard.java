package Baeksa.money.domain.auth.anotation;

import Baeksa.money.global.jwt.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component("authGuard")
@RequiredArgsConstructor
public class AuthGuard {

    private final JWTUtil jwtUtil;

    public boolean isApproved() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return false;

        String studentId = auth.getPrincipal().toString();
        String token = extractAccessToken(); // JWT 토큰 가져오기

        try {
            Claims claims = jwtUtil.validateJwt(token);
            String status = claims.get("status", String.class);
            return "APPROVE".equalsIgnoreCase(status);
        } catch (Exception e) {
            return false;
        }
    }

    private String extractAccessToken() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        return (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
    }
}

