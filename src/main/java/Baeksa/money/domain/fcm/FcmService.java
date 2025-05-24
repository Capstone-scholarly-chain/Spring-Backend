package Baeksa.money.domain.fcm;

import Baeksa.money.domain.auth.Service.MemberService;
import Baeksa.money.domain.auth.enums.Role;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final MemberService memberService;

    @Transactional
    public void saveFcmToken(String studentId, String fcmToken) {
        fcmTokenRepository.save(new FcmToken(studentId, fcmToken));
    }

//    @Transactional
//    public void addFcmToken(String studentId, String newToken) {
//        FcmToken fcmToken = fcmTokenRepository.findById(studentId).orElse(new FcmToken(studentId));
//
//        if (!fcmToken.getFcmTokens().contains(newToken)) {
//            fcmToken.getFcmTokens().add(newToken);
//            fcmTokenRepository.save(fcmToken);
//        }
//    }

    public void sendMessage(String fcmToken, String userId, String title, String body)
            throws FirebaseMessagingException {

        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            log.warn("⚠️ FCM 토큰이 비어있음: userId={}", userId);
            return;
        }

        // 메시지 생성
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .putData("userId", userId) // 추가 데이터
                .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        log.info("📱 FCM 전송 성공: userId={}, response={}", userId, response);
    }

    /**
     * userId로 FCM 토큰 조회 후 메시지 전송
     */
    public void sendMessageToUser(String userId, String title, String body) {
        try {
            String fcmToken = getFcmTokenByUserId(userId);
            if (fcmToken != null) {
                sendMessage(fcmToken, userId, title, body);
            } else {
                log.warn("⚠️ FCM 토큰을 찾을 수 없음: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("❌ 사용자 FCM 전송 실패: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 여러 사용자에게 FCM 메시지 일괄 전송
     */
    public void sendMessageToUsers(List<String> userIds, String title, String body) {
        List<String> fcmTokens = userIds.stream()
                .map(this::getFcmTokenByUserId)
                .filter(token -> token != null && !token.trim().isEmpty())
                .collect(Collectors.toList());

        if (!fcmTokens.isEmpty()) {
            sendMulticastMessage(fcmTokens, title, body);
        } else {
            log.warn("⚠️ 유효한 FCM 토큰이 없음: userIds={}", userIds);
        }
    }

    private String getFcmTokenByUserId(String userId) {
        Optional<FcmToken> fcmTokenOpt = fcmTokenRepository.findById(userId);
        if (fcmTokenOpt.isEmpty()) {
            log.warn("토큰 없음");
            return null;
        }
        FcmToken fcmToken = fcmTokenOpt.get();
        String token = fcmToken.getFcmToken();
        return token;
    }

    private List<String> getUserIdsByRole(Role role) {
        try {
            // 실제 구현에서는 UserRepository나 UserService를 통해 조회
            return memberService.getUserIdsByRole(role);
        } catch (Exception e) {
            log.error("❌ 역할별 사용자 조회 실패: role={}, error={}", role, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 학생회 구성원들에게 FCM 메시지 전송
     */
    public void sendMessageToCouncil(String title, String body) {
        try {
            List<String> councilUserIds = getUserIdsByRole(Role.ROLE_STUDENT);
            if (!councilUserIds.isEmpty()) {
                sendMessageToUsers(councilUserIds, title, body);
                log.info("📱 학생회 FCM 알림 전송 완료: 대상 {}명", councilUserIds.size());
            } else {
                log.warn("⚠️ 학생회 구성원을 찾을 수 없음");
            }
        } catch (Exception e) {
            log.error("❌ 학생회 FCM 전송 실패: error={}", e.getMessage());
        }
    }

    /**
     * 일반 학생들에게 FCM 메시지 전송
     */
    public void sendMessageToStudents(String title, String body) {
        try {
            List<String> studentUserIds = getUserIdsByRole(Role.ROLE_STUDENT);
            if (!studentUserIds.isEmpty()) {
                sendMessageToUsers(studentUserIds, title, body);
                log.info("📱 학생 FCM 알림 전송 완료: 대상 {}명", studentUserIds.size());
            } else {
                log.warn("⚠️ 일반 학생을 찾을 수 없음");
            }
        } catch (Exception e) {
            log.error("❌ 학생 FCM 전송 실패: error={}", e.getMessage());
        }
    }

//    public void sendMessage(String fcmToken, String userId, Role role, NotiType notiType) throws FirebaseMessagingException {
//
//        String title;
//        String body;
//
//        if (role.equals("ROLE_STUDENT")) {
//            title = "학생 " + userId + "님";
//
////                APPROVE_MEMBERSHIP, REJECT_MEMBERSHIP, COMMITTEE_APPLY_WITHDRAW,
////                STUDENT_APPLY_LEDGER, STUDENT_VOTE_WITHDRAW,
////                COMMITTEE_APPLY_WITHDRAW, COMMITTEE_APPROVE_DEPOSIT, COMMITTEE_REJECT_DEPOSIT
//            switch (notiType) {
//                case REGISTER_USER -> body = "회원가입 및 조직 가입 요청이 신청되었습니다.";
//                case APPROVE_MEMBERSHIP -> body = "조직 가입 요청이 승인되었습니다.";
//                case REJECT_MEMBERSHIP -> body = "조직 가입 요청이 거부되었습니다.";
//                case COMMITTEE_APPLY_WITHDRAW -> body = "학생회 출금 내역이 있습니다.";
////                case STUDENT_VOTE_WITHDRAW ->
//            }
//
//        } else {
//            title = "학생회 " + userId + "님";
//
//            switch (notiType) {
////                case REGISTER_USER -> body = "회원가입 및 조직 가입 요청이 있습니다.";
//                case APPROVE_MEMBERSHIP -> body = "조직 가입 요청이 승인되었습니다.";
//                case REJECT_MEMBERSHIP -> body = "조직 가입 요청이 거부되었습니다.";
////                case STUDENT_APPLY_LEDGER -> body = "입금 내역 등록이 신청되었습니다.";
//            }
//        }
//
//            // 메시지 생성
//        Notification notification = Notification.builder()
//                .setTitle(title)
//                .setBody(body)
//                .build();
//
//        Message message = Message.builder()
//                .setToken(fcmToken)  // FCM Token 사용
//                .setNotification(notification)
//                .build();
//
//        String response = FirebaseMessaging.getInstance().send(message);
//        log.info("FCM 전송 성공: {}", response);
//        }



    /**
     * 여러 FCM 토큰에 일괄 메시지 전송
     */
    private void sendMulticastMessage(List<String> fcmTokens, String title, String body) {
        try {
            if (fcmTokens.isEmpty()) {
                log.warn("⚠️ FCM 토큰 리스트가 비어있음");
                return;
            }

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(notification)
                    .addAllTokens(fcmTokens)
                    .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);

            log.info("📱 FCM 일괄 전송 완료: 성공 {}개, 실패 {}개",
                    response.getSuccessCount(), response.getFailureCount());

//            // 실패한 토큰 정리
//            if (response.getFailureCount() > 0) {
//                handleFailedTokens(fcmTokens, response);
//            }

        } catch (Exception e) {
            log.error("❌ FCM 일괄 전송 실패: error={}", e.getMessage());
        }
    }
}

//    public void addMessage(String token, String userId, Role role, NotiType notiType, Object result) {
//

//    }




//    public List<String> getFcmTokens(Long studentId) {
//        return fcmTokenRepository.findById(studentId)
//                .map(FcmToken::getFcmTokens)
//                .orElse(Collections.emptyList()); // 없으면 빈 리스트 반환
//    }
//
//    public void sendMessageToAll(List<String> fcmTokens, String title, String body)
//            throws FirebaseMessagingException {
//
//        Notification notification = Notification.builder()
//                .setTitle(title)
//                .setBody(body)
//                .build();
//
//        MulticastMessage message = MulticastMessage.builder()
//                .setNotification(notification)
//                .addAllTokens(fcmTokens)
//                .build();
//
//        //fcm에서 제공하는 BatchResponse. 실패카운트, 성공카운트가 있다.
//        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
//
//        log.info("FCM 전송 완료: 성공 {}개, 실패 {}개",
//                response.getSuccessCount(), response.getFailureCount());
//
////        // 실패한 토큰 정리도 가능
////        if (response.getFailureCount() > 0) {
////            List<String> failedTokens = new ArrayList<>();
////            List<SendResponse> responses = response.getResponses();
////            for (int i = 0; i < responses.size(); i++) {
////                if (!responses.get(i).isSuccessful()) {
////                    failedTokens.add(fcmTokens.get(i));
////                }
////            }
////            log.warn("FCM 전송 실패 토큰들: {}", failedTokens);
////            // 여기서 DB나 Redis에서 무효 토큰 삭제해도 됨
////        }
//    }


