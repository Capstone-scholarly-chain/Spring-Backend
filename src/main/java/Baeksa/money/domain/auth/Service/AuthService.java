package Baeksa.money.domain.auth.Service;

import Baeksa.money.domain.auth.Dto.MemberDto;
import Baeksa.money.domain.auth.enums.Role;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.jwt.CustomUserDetails;
import Baeksa.money.global.jwt.JWTUtil;
import Baeksa.money.global.redis.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public MemberDto.LoginResponse login(String header, HttpServletResponse response) {

        if (header == null || !header.startsWith("Basic ")) {
            throw new CustomException(ErrorCode.INVALID_AUTH_HEADER);
        }

        // 1. Basic 헤더 디코딩
        String base64Credentials = header.substring("Basic ".length());
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String[] values = credentials.split(":", 2);

        if (values.length != 2) {
            throw new CustomException(ErrorCode.INVALID_AUTH_HEADER2);
        }

        String studentId = values[0];
        String password = values[1];

        // 2. 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(studentId, password)
        );

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String role = customUserDetails.getAuthorities().iterator().next().getAuthority();

        // 3. JWT 발급
        String accessToken = jwtUtil.createJwt("access", studentId, role,
                role.equals("ROLE_ADMIN") ? 21600000L : 600000L); // 6시간 or 10분

        String refreshToken;
        if (refreshTokenService.existsRefresh(studentId)) {
            refreshToken = refreshTokenService.getToken(studentId);
        } else {
            refreshToken = jwtUtil.createJwt("refresh", studentId, role, 86400000L); // 24시간
            refreshTokenService.save(studentId, refreshToken);
        }

        // 4. 쿠키 설정
        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addCookie(refreshTokenService.createCookie("access_token", accessToken));
        response.addCookie(refreshTokenService.createCookie("refresh_token", refreshToken));

        Role roleStr = Role.valueOf(role);  //enum을 string변환한걸 다시 enum으로
        MemberDto.LoginResponse responseDto = MemberDto.LoginResponse.builder()
                .username(customUserDetails.getUsername())
                .studentId(customUserDetails.getStudentId())
                .role(roleStr)
                .build();

        return responseDto;
    }
}
