package Baeksa.money.domain.register.controller;

import Baeksa.money.domain.register.service.RegisterService;
import Baeksa.money.global.config.swagger.ApiErrorCodeExample;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pubsub/register")
@Tag(name = "진행중인 조직 가입 요청 조회 API")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

    ///테스트 성공
    @ApiErrorCodeExample(value = ErrorCode.class, include = {""})
    @Operation(summary = "모든 대기중인 회원가입(조직 가입) 요청 조회")
    @GetMapping("/pending-requests")
    public ResponseEntity<?> getPendingRequests(){

        List<Map<String, Object>> list = registerService.findPendingRequests();
        return ResponseEntity.ok(new BaseApiResponse<>(200, "FIND-PENDING-REQUESTS", "모든 대기중인 회원가입(조직 가입) 요청 조회", list));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {""})
    @Operation(summary = "특정 id 회원가입(조직 가입) 요청 상태 조회")
    @GetMapping("/register-status")
    public ResponseEntity<?> getIdStatusRequests(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, Object> list = registerService.findRegisterUserStatus(userDetails.getStudentId());   //로그인된 사람의 요청 상태 조회
        return ResponseEntity.ok(new BaseApiResponse<>(200, "FIND-ID-PENDING-REQUESTS", "특정 id 회원가입(조직 가입) 요청 상태 조회", list));
    }
}
