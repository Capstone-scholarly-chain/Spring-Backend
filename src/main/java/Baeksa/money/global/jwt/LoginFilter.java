package Baeksa.money.global.jwt;


import Baeksa.money.domain.auth.enums.Role;
import Baeksa.money.global.redis.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.io.IOException;

//@Slf4j
//@AllArgsConstructor
//public class LoginFilter extends UsernamePasswordAuthenticationFilter {
//
//    private final AuthenticationManager authenticationManager;
//    private final JWTUtil jwtUtil;
//    private final RefreshTokenService refreshTokenService;
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
//
//        try {
//            String authorizationHeader = request.getHeader("Authorization");
//
//            if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
//                throw new RuntimeException("Authorization header is missing or incorrect.");
//            }
//            log.info("header:" , authorizationHeader);
//            if (authorizationHeader.startsWith("Basic ")){
//                String base64Credentials = authorizationHeader.substring("Basic ".length());
//                String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
//                String[] values = credentials.split(":", 2);
//                String studentId = values[0];
//                String password = values[1];
//
//            //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
//            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(studentId,
//                    password, null);
//
//            //token에 담은 검증을 위한 AuthenticationManager로 전달
//            return authenticationManager.authenticate(authToken);
//            } else {
//                throw new RuntimeException("Authorization header is missing or incorrect.");
//            }
//        } catch (Exception e) {
//            System.out.println("Authentication error: " + e.getMessage());
//            throw new RuntimeException("Failed to read authorization header", e);
//        }
//    }
//
//    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
//                                            FilterChain chain, Authentication authentication) throws IOException, ServletException {
//
//        //토큰 두개 - access, refresh
//        //유저 정보
//        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
//
////        String username = authentication.getName();
//        String studentId = customUserDetails.getStudentId();
//        String role = authentication.getAuthorities().iterator().next().getAuthority();
//
//        String access_token;
//        // access token 항상 새로 발급 - 액세스 10분
//        if (role.equals(Role.ROLE_ADMIN.name())){   //.name()은 enum을 문자열로 해줌
//            access_token = jwtUtil.createJwt("access", studentId, role, 21600000L);    //6시간
//        }else {
//            access_token = jwtUtil.createJwt("access", studentId, role, 600000L); // 10분 = 10 * 60 * 1000
//        }
//
//        // 이미 발급되었으면 refresh 발급 안합니다
//        String refresh_token;
//        if (refreshTokenService.existsRefresh(studentId)){
//            refresh_token = refreshTokenService.getToken(studentId);
//        }
//        else{
//            refresh_token = jwtUtil.createJwt("refresh", studentId, role, 86400000L);
//            refreshTokenService.save(studentId, refresh_token);   // Redis에 저장!!!!!!!
//        }
//
//        //헤더에 액세스 토큰, 쿠키에 리프레시, 설정되면 ok
//// Authorization 헤더에 access token 설정
//        response.addHeader("Authorization", "Bearer " + access_token);
//        response.addCookie(refreshTokenService.createCookie("access_token", access_token));
//        response.addCookie(refreshTokenService.createCookie("refresh_token", refresh_token));
//
//        //이건 void 오버라이드라 응답을 내가 만들어야함
//        //JSON응답 설정
//        //아니면 오브젝트매퍼로 만들어
//        response.setStatus(HttpStatus.OK.value());
//        response.setContentType("application/json;charset=UTF-8");
//
//        String jsonResponse = String.format(
//                "{ \"status\": 200, \"message\": \"로그인 성공\", \"studentId\": %s, \"role\": \"%s\" }", studentId, role
//        );
//
//        response.getWriter().write(jsonResponse);
//    }
//
//    //로그인 실패시 실행하는 메소드
//    @Override
//    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
//                                              AuthenticationException failed) throws IOException {
//
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
//        response.setContentType("application/json;charset=UTF-8");
//
//        String jsonResponse = String.format(
//                "{ \"status\": 401, \"message\": \"로그인 실패\" }" );
//
//        response.getWriter().write(jsonResponse);
//    }
//}
