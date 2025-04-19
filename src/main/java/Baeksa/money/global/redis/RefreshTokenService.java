package Baeksa.money.global.redis;

import Baeksa.money.domain.Dto.MemberDto;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.ErrorCode;
import Baeksa.money.global.jwt.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisConnectionFactory redisConnectionFactory;

    @Transactional
    public void save(Long studentId, String token) {
        refreshTokenRepository.save(new RefreshToken(studentId, token));
    }

    @Transactional
    public Optional<RefreshToken> find(Long studentId) {
        return refreshTokenRepository.findById(studentId);
    }

    @Transactional
    public void logout(Long studentId) {
        if (studentId != null) {
            // ID가 null이 아닌 경우에만 삭제 작업을 진행
            refreshTokenRepository.deleteById(studentId);
        } else {
            // ID가 null인 경우 적절한 예외 처리나 로그를 추가할 수 있음
            throw new CustomException(ErrorCode.INVALID_ID);
        }
    }


    public MemberDto.TokenResponse refreshValid(HttpServletRequest request) {

        //1. 쿠키에서 refresh 토큰 꺼내기
        String refresh = getCookieValue(request.getCookies(), "refresh");
        if (refresh == null) {
            throw new CustomException(ErrorCode.TOKEN_NOTFOUND);
        }

        //2. 만료 여부 체크
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        //3. 카테고리 체크
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        //4. redis 토큰과 비교
        Optional<RefreshToken> redisTokenOpt = refreshTokenRepository.findById(jwtUtil.getStudentId(refresh));
        if (redisTokenOpt.isEmpty() || !redisTokenOpt.get().getRefreshToken().equals(refresh)){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String username = jwtUtil.getUsername(refresh);
        Long studentId = jwtUtil.getStudentId(refresh);
        String role = jwtUtil.getRole(refresh);

        return new MemberDto.TokenResponse(username, studentId, role);
    }

    public ResponseEntity<?> reissue(HttpServletResponse response, MemberDto.TokenResponse tokenResponse) {
        // 5. 기존 refresh삭제
        refreshTokenRepository.deleteById(tokenResponse.getStudentId());

        // 6. 재발급
        String newAccess = jwtUtil.createJwt("access", tokenResponse.getUsername(), tokenResponse.getStudentId(), tokenResponse.getRole(), 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", tokenResponse.getUsername(), tokenResponse.getStudentId(), tokenResponse.getRole(), 86400000L);

        // 7. Redis 저장 (rotate)
        refreshTokenRepository.save(new RefreshToken(tokenResponse.getStudentId(), newRefresh));

        // 8. 쿠키로 응답
        response.addCookie(createAccessCookie("access", newAccess));
        response.addCookie(createRefreshCookie("refresh", newRefresh));

        return ResponseEntity.ok("토큰 재발급 완료");
    }


    //access에서도 사용함 - JWTFilter
    public String getCookieValue(Cookie[] cookies, String key) {
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(key)) return cookie.getValue();
        }
        return null;
    }

    private Cookie createAccessCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 10);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }

    private Cookie createRefreshCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 24);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }


    //LoginFilter에서 사용함
    public String getToken(Long studentId) {
        RedisConnection connection = redisConnectionFactory.getConnection();
        String key = "token:" + studentId; // redis String을 쓰라고 하네요
        byte[] bytes = connection.hGet(key.getBytes(), "refreshToken".getBytes());  //byte타입만 있네요
        if (bytes == null) {
            return null;
        }
        return new String(bytes);
    }

    public Long getTtl(Long studentId) {
        RedisConnection connection = redisConnectionFactory.getConnection();
        String key = "token:" + studentId; // redis String을 쓰라고 하네요
        return connection.ttl(key.getBytes()); // 초 단위 TTL 반환
    }

    public boolean existsRefresh(Long studentId) {
        Optional<RefreshToken> tokenOptional = refreshTokenRepository.findById(studentId);
        if (tokenOptional.isEmpty()) {
            return false;
        }
        Long ttl = getTtl(studentId);
        return ttl != null && ttl > 300;    //5분이상 남았을때
    }


}



