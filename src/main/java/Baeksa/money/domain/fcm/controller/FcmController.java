package Baeksa.money.domain.fcm.controller;

import Baeksa.money.domain.fcm.service.FcmService;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/fcm")
@RestController
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @Operation(summary = "알림 기능 test API - 단일 유저", description = "sendMessageToUser")
    @PostMapping("/test")
    public ResponseEntity<?> test(@RequestBody String userId,
                                  @RequestBody String title,
                                  @RequestBody String body){

        fcmService.sendMessageToUser(userId, title, body);

        return ResponseEntity.ok(new BaseApiResponse(200, "FCM-SEND", "fcm 알림 전송", null));
    }

    @Operation(summary = "알림 기능 test API - 토큰 리스트", description = "sendMessageToStudents, sendMessageToCouncil, " +
            "adminListTokens, studentListTokens, committeeListTokens")
    @PostMapping("/test2")
    public ResponseEntity<?> test2(@RequestBody String title,
                                   @RequestBody String body){

            //❌ FCM 일괄 전송 실패: error=Unexpected HTTP response with status: 404

            // 1단계: 단일 메시지 테스트
//            fcmService.testSingleMessage();

            // 2단계: 성공하면 멀티캐스트 테스트
        fcmService.adminListTokens(title, body);

        return ResponseEntity.ok(new BaseApiResponse(200, "FCM-SEND", "fcm 알림 전송", null));
    }
}
