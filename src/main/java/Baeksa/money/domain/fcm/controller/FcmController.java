package Baeksa.money.domain.fcm.controller;

import Baeksa.money.domain.fcm.service.FcmService;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/fcm")
@RestController
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @Operation(summary = "test API")
    @PostMapping("/test")
    public ResponseEntity<?> test(){

        fcmService.sendMessageToUser("202212345", "알림 테스트", "됐나?");

        return ResponseEntity.ok(new BaseApiResponse(200, "FCM-SEND", "fcm 알림 전송", null));
    }

    @Operation(summary = "test2 API")
    @PostMapping("/test2")
    public ResponseEntity<?> test2(){

            //❌ FCM 일괄 전송 실패: error=Unexpected HTTP response with status: 404

            // 1단계: 단일 메시지 테스트
//            fcmService.testSingleMessage();

            // 2단계: 성공하면 멀티캐스트 테스트
        fcmService.adminListTokens("모모모", "훈훈훈크크크");

        return ResponseEntity.ok(new BaseApiResponse(200, "FCM-SEND", "fcm 알림 전송", null));
    }
}
