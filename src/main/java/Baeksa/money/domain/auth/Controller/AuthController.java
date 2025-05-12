package Baeksa.money.domain.auth.Controller;


import Baeksa.money.domain.auth.Dto.MemberDto;
import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.Service.AuthService;
import Baeksa.money.domain.auth.Service.MemberService;
import Baeksa.money.domain.auth.Service.StudentService;
import Baeksa.money.domain.auth.converter.MemberConverter;
import Baeksa.money.domain.auth.exception.AuthErrorCode;
import Baeksa.money.global.config.swagger.ApiErrorCodeExample;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.excepction.code.ErrorResponse;
import Baeksa.money.global.jwt.CustomUserDetails;
import Baeksa.money.global.jwt.JWTUtil;
import Baeksa.money.global.redis.RedisDto;
import Baeksa.money.domain.committee.service.CommitteePublisher;
import Baeksa.money.global.redis.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;


//회원가입 & 검증용 컨트롤러
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입 & 로그인 관련 API")
public class AuthController {

    private final MemberService memberService;
    private final StudentService studentService;
    private final MemberConverter memberConverter;
    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;
    private final CommitteePublisher committeePublisher;
    private final AuthService authService;


    @Operation(summary = "회원가입 API")
    @ApiErrorCodeExample(
            value = ErrorCode.class,
            include = {"STUDENT_NOTFOUND", "DUPICATED_MEMBER", "DUPLICATED_MEMBER", "PASSWORD_NOT_MATCH", "INVALID_ACCESS"}
    )
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody MemberDto memberDto) {

        //학생정보가 맞는지 studentService에서 검증 로직 실행
        try {
            boolean ValidStudent = studentService.signupValid(
                    memberDto.getStudentId(),
                    memberDto.getUsername(),
                    memberDto.getPhoneNumber(),
                    memberDto.getRole()
            );
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

            Map<String, Object> sendDto = new HashMap<>();
            sendDto.put("userId", memberDto.getStudentId());
            sendDto.put("name", memberDto.getUsername());
            sendDto.put("orgType", memberDto.getRole());
            //폰빼고 보내기
            committeePublisher.publish("spring:request:register-user", savedDto);

            //가입된 회원의 정보를 반환
            return ResponseEntity.ok(new BaseApiResponse<>(201, "SIGNUP", "회원가입 완료", savedDto));

            // 커스텀 예외는 그대로 던져서 글로벌 예외처리기에서 처리되게
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER);
        }
    }

    ///
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestHeader("Authorization") String header, HttpServletResponse response) {
        MemberDto.LoginResponse loginResponse = authService.login(header, response);

        return ResponseEntity.ok(
                new BaseApiResponse<>(200, "LOGIN", "로그인", loginResponse)
        );
    }

    //requestId, userId, name, orgType받기
    //REQ_STUDENT
    //키: membership:request:* 로 읽으면 값 REQ_COUNCIL
    @ApiErrorCodeExample(
            value = ErrorCode.class,
            include = {"EXPIRED_TOKEN", "INVALID_TOKEN", "TOKEN_NOTFOUND"}
    )
    @Operation(summary = "access, refresh토큰 재발행")
    @PostMapping("/api/auth/reissue")
    public ResponseEntity<?> reissue(@RequestBody RedisDto.Refresh refresh_token,
                                   HttpServletResponse response) {

        RedisDto.TokenResponse tokenResponse = refreshTokenService.refreshValid(refresh_token);
        refreshTokenService.reissue(response, tokenResponse);

        return ResponseEntity.ok(
                new BaseApiResponse<>(200, "REISSUE", "재발행", tokenResponse)
        );
    }

    @ApiErrorCodeExample(
            value = ErrorCode.class,
            include = {"INVALID_ACCESS", "BLACKLISTED", "EXPIRED_TOKEN", "INTERNAL_SERVER_ERROR"}
    )
    @Operation(summary = "로그아웃 API")
    @PostMapping("/api/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {

        try {
            // 헤더에서 access 꺼내기
//        String accessToken = refreshTokenService.extractAccessFromHeader(request);
            String accessToken = refreshTokenService.getCookieValue(request.getCookies(), "access_token");
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


            //로그아웃 전에 refresh 검증할 필요가 있을까? -> 만료 여부만 체크
            String refresh = refreshTokenService.getCookieValue(request.getCookies(), "refresh_token");
            try {
                jwtUtil.isExpired(refresh);
            } catch (ExpiredJwtException e) {
                throw new CustomException(ErrorCode.EXPIRED_TOKEN);
            }
            log.info("refresh: {}", refresh);

            //해당 refresh토큰 삭제
            String studentId = jwtUtil.getStudentId(accessToken);
            refreshTokenService.logout(studentId);

            //쿠키 삭제
            refreshTokenService.deleteCookie(response, new Cookie("access_token", null));
            refreshTokenService.deleteCookie(response, new Cookie("refresh_token", null));

            return ResponseEntity.ok(new BaseApiResponse<>(200, "LOGOUT", "로그아웃 성공", null));
        }

        catch (Exception e) {
            e.printStackTrace(); // 에러 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("로그아웃 처리 중 오류가 발생했습니다.");
        }

    }

}
