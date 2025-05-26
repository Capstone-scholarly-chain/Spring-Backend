package Baeksa.money.domain.auth.Controller;


import Baeksa.money.domain.auth.Dto.MemberDto;
import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.Service.AuthService;
import Baeksa.money.domain.auth.Service.MemberService;
import Baeksa.money.domain.auth.Service.StudentValidService;
import Baeksa.money.domain.auth.converter.MemberConverter;
import Baeksa.money.global.config.swagger.ApiErrorCodeExample;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.jwt.JWTUtil;
import Baeksa.money.global.redis.RedisDto;
import Baeksa.money.domain.committee.service.CommitteePublisher;
import Baeksa.money.global.redis.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;

    @Operation(summary = "회원가입 API")
    @ApiErrorCodeExample(
            value = ErrorCode.class,
            include = {"STUDENT_NOTFOUND", "DUPLICATED_MEMBER", "DUPLICATED_MEMBER", "PASSWORD_NOT_MATCH", "INVALID_ACCESS"}
    )
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody MemberDto memberDto) {

        MemberDto.MemberResponseDto savedDto = authService.signup(memberDto);
        return ResponseEntity.ok(new BaseApiResponse<>(201, "SIGNUP", "회원가입 완료", savedDto));
    }

    @Operation(summary = "로그인 API")
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
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response) {

        authService.logout(request, response);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "LOGOUT", "로그아웃 성공", null));
    }


}
