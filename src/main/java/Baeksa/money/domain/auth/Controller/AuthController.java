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
//   public List<Map<String, Object>> getPendingDeposits() {
//        try {
//            // 🔥 Redis에서 JSON 문자열로 저장된 데이터 조회
//            String cachedData = redisTemplate.opsForValue().get("ledger:pending-deposits");
//            log.info("이건읽냐");
//
//            if (cachedData != null && !cachedData.isEmpty()) {
//                try {
//                    // JSON 문자열을 List<Map>으로 파싱
//                    List<Map<String, Object>> result = objectMapper.readValue(
//                            cachedData,
//                            new TypeReference<List<Map<String, Object>>>() {}
//                    );
//                    log.info("캐시에서 대기중인 입금 요청 조회: {} 건", result.size());
//                    return result;
//                } catch (Exception e) {
//                    log.warn("캐시된 입금 요청 데이터 파싱 실패, 재요청: {}", e.getMessage());
//                    redisTemplate.delete("ledger:pending-deposits"); // 잘못된 데이터 삭제
//                }
//            }
//
//            // 캐시에 없거나 파싱 실패 시 NestJS에 요청
//            log.info("캐시에 대기중인 입금 요청 없음, NestJS에 요청");
//            return requestPendingDeposits();
//
//        } catch (Exception e) {
//            log.error("대기중인 입금 항목 조회 실패", e);
//            throw new CustomException(ErrorCode.PENDING_DEPOSIT_FETCH_FAILED);
//        }
//    }
//
//    private List<Map<String, Object>> requestPendingDeposits() throws InterruptedException, JsonProcessingException {
//        String recordId = redisStreamProducer.sendMessage("대기중인 입금 요청", "GET_PENDING_DEPOSITS").toString();
//        CountDownLatch latch = requestTracker.registerRequest(recordId);
//
//        try {
//            log.info("대기중인 입금 요청 발송: recordId={}", recordId);   //여기까지 로그
//            //nest가 보낸 거 받는데, 파싱 실패
//
//            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
//
//            if (!receivedInTime) {
//                log.warn("대기중인 입금 요청 응답 타임아웃: recordId={}", recordId);
//                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
//            }
//
//            if (!requestTracker.isRequestSuccessful(recordId)) {
//                log.warn("대기중인 입금 요청 응답 실패: recordId={}", recordId);
//                throw new CustomException(ErrorCode.REQUEST_FAILED);
//            }
//
//            // 응답 데이터 조회
//            String data = redisTemplate.opsForValue().get("ledger:pending-deposits");
//
//            if (data == null) {
//                log.warn("대기중인 입금 요청 응답 후에도 데이터 없음: recordId={}", recordId);
//                return new ArrayList<>(); // 빈 리스트 반환
//            }
//
//            // JSON 문자열을 List<Map>으로 파싱
//            return objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {});
//
//        } finally {
//            requestTracker.cleanupRequest(recordId);
//        }
//    }

}
