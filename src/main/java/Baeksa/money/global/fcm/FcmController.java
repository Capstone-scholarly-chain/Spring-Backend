package Baeksa.money.global.fcm;

import Baeksa.money.domain.Dto.MemberDto;
import Baeksa.money.domain.Entity.MemberEntity;
import Baeksa.money.domain.Service.MemberService;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.ErrorCode;
import Baeksa.money.global.jwt.CustomUserDetails;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/fcm")
@RestController
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/token")
    public ResponseEntity<?> saveToken(@RequestBody FcmRequestDto fcmRequestDto,
                                       @AuthenticationPrincipal CustomUserDetails userDetails){

        //로그인 후 앱 실행에서 발급
        Long studentId = userDetails.getStudentId();

        //fcm token을 DB에 저장하기
        fcmService.addFcmToken(studentId, fcmRequestDto.getFcmToken());

        return ResponseEntity.ok(new MemberDto.ApiResponse(200, "FCM-TOKEN", "fcm 토큰 저장 완료", null));
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody FcmRequestDto fcmRequestDto,
                                         @AuthenticationPrincipal CustomUserDetails userDetails){

        Long studentId = userDetails.getStudentId();
        List<String> fcmTokens = fcmService.getFcmTokens(studentId);
        if (fcmTokens.isEmpty()) {
            throw new CustomException(ErrorCode.FCMTOKEN_NOTFOUND);
        }
        String title = fcmRequestDto.getTitle();
        String body = fcmRequestDto.getBody();
        try {
            fcmService.sendMessageToAll(fcmTokens, title, body);
//            fcmService.sendMessage(fcmRequestDto.getFcmToken(), title, body);                ,

        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.FCM_SEND_FAIL);
        }
        return ResponseEntity.ok(new MemberDto.ApiResponse(200, "FCM-SEND", "fcm 알림 전송", null));
    }

}
