package Baeksa.money.domain.auth.Service;

import Baeksa.money.domain.auth.Dto.MemberDto;
import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.converter.MemberConverter;
import Baeksa.money.domain.auth.enums.Role;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.jwt.CustomUserDetails;
import Baeksa.money.global.jwt.JWTUtil;
import Baeksa.money.global.redis.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final MemberService memberService;
    private final StudentValidService studentValidService;
    private final MemberConverter memberConverter;
    private final AuthPublisher authPublisher;

    public MemberDto.MemberResponseDto signup(MemberDto memberDto) {

        String studentId = memberDto.getStudentId();
        String username = memberDto.getUsername();
        String phoneNumber = memberDto.getPhoneNumber();
        Role role = memberDto.getRole();

        try {
            boolean ValidStudent = studentValidService.signupValid(
                    studentId, username, phoneNumber, role);
            //아닐경우 학생정보 찾을 수 없다는 커스텀에러
            if (!ValidStudent) {
                throw new CustomException(ErrorCode.STUDENT_NOTFOUND);
            }
            //회원가입 서비스 호출 - 여기서 중복회원, 비밀번호 2차
            MemberEntity savedEntity = memberService.signup(memberDto);

            //savedEntity를 MemberDto로 변환하여 반환해야됨(컨트롤러니깐)
            MemberDto.MemberResponseDto savedDto = memberConverter.toResponseDto(savedEntity);
            log.info("savedDto: {}", savedDto.getStudentId());
            log.info("memberDto: {}", memberDto.getStudentId());

            authPublisher.publishSignup(studentId, username, role);

            return savedDto;

            // 커스텀 예외는 그대로 던져서 글로벌 예외처리기에서 처리되게
        } catch (CustomException e) {
            throw e;

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER);
        }
    }


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
        String username = customUserDetails.getRealUsername();  //실제 사용자 이름 가져옴

        // 3. JWT 발급
        String accessToken = jwtUtil.createJwt("access", studentId, username, role,
                role.equals("ROLE_ADMIN") ? 21600000L : 600000L); // 6시간 or 10분

        String refreshToken;
        if (refreshTokenService.existsRefresh(studentId)) {
            refreshToken = refreshTokenService.getToken(studentId);
        } else {
            refreshToken = jwtUtil.createJwt("refresh", studentId, username, role, 86400000L); // 24시간
            refreshTokenService.save(studentId, refreshToken);
        }

        // 4. 쿠키 설정
        response.addCookie(refreshTokenService.createCookie("access_token", accessToken));
        response.addCookie(refreshTokenService.createCookie("refresh_token", refreshToken));

        response.addHeader("Authorization", "Bearer " + accessToken);
        Role roleStr = Role.valueOf(role);  //enum을 string변환한걸 다시 enum으로
        MemberDto.LoginResponse responseDto = MemberDto.LoginResponse.builder()
                .username(customUserDetails.getRealUsername())
                .studentId(customUserDetails.getStudentId())
                .role(roleStr)
                .build();

        return responseDto;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {

        try {
            // 헤더에서 access 꺼내기
            String accessToken = extractAccessFromHeader(request);

            log.info("access: {}", accessToken);

            if (accessToken == null || accessToken.trim().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_ACCESS); // 적절한 에러코드로 대체
            }

            //블랙리스트 처리된 access 토큰인지 검증
            if (refreshTokenService.isBlacklisted(accessToken)) {
                throw new CustomException(ErrorCode.BLACKLISTED);
            }
            /// 아니면 이제 access 블랙리스트 처리해
            refreshTokenService.blacklist(accessToken, jwtUtil.getExpiration(accessToken));

            //해당 refresh토큰 삭제
            String studentId = jwtUtil.getStudentId(accessToken);
            refreshTokenService.logout(studentId);

            //쿠키 삭제
            refreshTokenService.deleteCookie(response, new Cookie("access_token", null));
            refreshTokenService.deleteCookie(response, new Cookie("refresh_token", null));
        }

        catch (Exception e) {
            throw new CustomException(ErrorCode.LOGOUT);
        }
    }

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
