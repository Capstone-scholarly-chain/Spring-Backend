package Baeksa.money.global.jwt;


import Baeksa.money.global.redis.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Map;
import java.io.IOException;


@AllArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> credentials = objectMapper.readValue(request.getInputStream(), Map.class);

            //클라이언트 요청에서 studentId, password 추출
            //json의 키는 항상 String이기에 string으로 해줘야하는듯
            Long studentId = Long.valueOf(credentials.get("studentId").toString());
            String password = credentials.get("password").toString();

            //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(studentId.toString(), password, null);

            //token에 담은 검증을 위한 AuthenticationManager로 전달
            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {

        //토큰 두개 - access, refresh
        //유저 정보
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String username = authentication.getName();
        Long studentId = customUserDetails.getStudentId();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // access token 항상 새로 발급 - 액세스 10분
        String access = jwtUtil.createJwt("access", username, studentId, role, 600000L);

        // 이미 발급되었으면 refresh 발급 안합니다
        String refresh;
        if (refreshTokenService.existsRefresh(studentId)){
            refresh = refreshTokenService.getToken(studentId);
        }
        else{
            refresh = jwtUtil.createJwt("refresh", username, studentId, role, 86400000L);

            // Redis에 저장!!!!!!!
            refreshTokenService.save(studentId, refresh);
        }

        //헤더에 액세스 토큰, 쿠키에 리프레시, 설정되면 ok
//        response.setHeader("access", access);
        response.addCookie(createAccessCookie("access", access));
        response.addCookie(createRefreshCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {

        response.setStatus(401);
    }

    //쿠키
    private Cookie createAccessCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 10); // access는 10분짜리라면 여기에 맞게도 가능
        cookie.setHttpOnly(true);  // JS에서 접근 못하게
        cookie.setSecure(true);    // HTTPS에서만 전송 (개발 중엔 생략 가능)
        cookie.setPath("/");       // 모든 경로에 대해 전송
        return cookie;
    }

    private Cookie createRefreshCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }
}
