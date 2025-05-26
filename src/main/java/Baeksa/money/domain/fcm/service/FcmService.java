package Baeksa.money.domain.fcm.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {
    
    private final FcmFirestoreService fcmFirestoreService;

    // FcmService에서 테스트
    public void adminListTokens(String title, String body) {
        try {
            List<String> listTokens = fcmFirestoreService.getTokensFromListField("ROLE_ADMIN");
            log.info("admin list에서 가져온 토큰들: {}", listTokens);

            if (!listTokens.isEmpty()) {
                sendMulticastMessage(listTokens, title, body);
            }
        } catch (Exception e) {
            log.error("admin list 실패: {}", e.getMessage());
        }
    }

    public void studentListTokens(String title, String body) {
        try {
            List<String> listTokens = fcmFirestoreService.getTokensFromListField("ROLE_STUDENT");
            log.info("student list에서 가져온 토큰들: {}", listTokens);

            if (!listTokens.isEmpty()) {
                sendMulticastMessage(listTokens, title, body);
            }
        } catch (Exception e) {
            log.error("student list 실패: {}", e.getMessage());
        }
    }

    public void committeeListTokens(String title, String body) {
        try {
            List<String> listTokens = fcmFirestoreService.getTokensFromListField("ROLE_COMMITTEE");
            log.info("committee list에서 가져온 토큰들: {}", listTokens);

            if (!listTokens.isEmpty()) {
                sendMulticastMessage(listTokens, title, body);
            }
        } catch (Exception e) {
            log.error("committee list 실패: {}", e.getMessage());
        }
    }

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
//                throw new CustomException(ErrorCode.FCMTOKEN_NOTFOUND);
            }
        } catch (Exception e) {
            log.error("❌ 사용자 FCM 전송 실패: userId={}, error={}", userId, e.getMessage());
//            throw new CustomException(ErrorCode.FCM_SEND_FAIL);
        }
    }

//    /**
//     * 여러 사용자에게 FCM 메시지 일괄 전송
//     */
//    public void sendMessageToUsers(List<String> userIds, String title, String body) {
//        List<String> fcmTokens = userIds.stream()
//                .map(this::getFcmTokenByUserId)
//                .filter(token -> token != null && !token.trim().isEmpty())
//                .collect(Collectors.toList());
//
//        if (!fcmTokens.isEmpty()) {
//            sendMulticastMessage(fcmTokens, title, body);
//        } else {
//            log.warn("⚠️ 유효한 FCM 토큰이 없음: userIds={}", userIds);
//        }
//    }

    private String getFcmTokenByUserId(String userId) {
        try {
            String tokenByStudentId = fcmFirestoreService.getTokenByStudentId(userId);
            log.info("[ userId: {}, tokenByStudentId: {} ]", userId, tokenByStudentId);
            return tokenByStudentId;
        } catch (Exception e) {
            log.error("FCM 없음: error={}", e.getMessage());
            return null;
            /// //////////이거 null로햇당
        }
    }

    /**
     * 학생회 구성원들에게 FCM 메시지 전송
     */
    public void sendMessageToCouncil(String title, String body) {
        try {
            List<String> councilTokens = fcmFirestoreService.getAllTokensByCommittee();
            if (!councilTokens.isEmpty()) {
                sendMulticastMessage(councilTokens, title, body);
                log.info("sendMessageToCouncil 학생회 FCM 알림 전송 완료: 대상 {}명", councilTokens.size());
            } else {
                log.warn("sendMessageToCouncil 학생회 구성원을 찾을 수 없음");
            }
        } catch (Exception e) {
            log.error("sendMessageToCouncil 학생회 FCM 전송 실패: error={}", e.getMessage());
//            throw new CustomException(ErrorCode.FCM_SEND_FAIL);
        }
    }

    /**
     * 일반 학생들에게 FCM 메시지 전송
     */
    public void sendMessageToStudents(String title, String body) {
        try {
            List<String> studentTokens = fcmFirestoreService.getAllTokensByStudent();
            if (!studentTokens.isEmpty()) {
                sendMulticastMessage(studentTokens, title, body);
                log.info("sendMessageToStudents 학생 FCM 알림 전송 완료: 대상 {}명", studentTokens.size());
            } else {
                log.warn("sendMessageToStudents 일반 학생을 찾을 수 없음");
            }
        } catch (Exception e) {
            log.error("sendMessageToStudents 학생 FCM 전송 실패: error={}", e.getMessage());
//            throw new CustomException(ErrorCode.FCM_SEND_FAIL);
        }
    }

    //Admin
    public void sendMessageToAdmin(String title, String body) {
        try {
            List<String> adminTokens = fcmFirestoreService.getAllTokensByAdmin();
            if (!adminTokens.isEmpty()) {
                sendMulticastMessage(adminTokens, title, body); // 직접 멀티캐스트 호출
                log.info("sendMessageToAdmin ADMIN FCM 알림 전송 완료: 대상 {}명", adminTokens.size());
            } else {
                log.warn("sendMessageToAdmin ADMIN을 찾을 수 없음");
            }
        } catch (Exception e) {
            log.error("sendMessageToAdmin ADMIN FCM 전송 실패: error={}", e.getMessage());
//            throw new CustomException(ErrorCode.FCM_SEND_FAIL);
        }
    }

    /**
     * 여러 FCM 토큰에 일괄 메시지 전송
     */
    private void sendMulticastMessage(List<String> fcmTokens, String title, String body) {
        try {
            if (fcmTokens.isEmpty()) {
                log.warn("sendMulticastMessage FCM 토큰 리스트가 비어있음");
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

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            log.info("FCM 일괄 전송 완료: 성공 {}개, 실패 {}개",
                    response.getSuccessCount(), response.getFailureCount());

        } catch (Exception e) {
            log.error("sendMulticastMessage FCM 일괄 전송 실패: error={}", e.getMessage());

            // 원인 분석을 위한 추가 로그
            if (e.getCause() != null) {
                log.error("sendMulticastMessage 근본 원인: {}", e.getCause().getMessage());
            }
        }
    }

    public void testSingleMessage() {
        try {
            List<String> tokens = fcmFirestoreService.getTokensFromListField("ROLE_ADMIN");
            if (tokens.isEmpty()) {
                log.warn("토큰이 없음");
                return;
            }

            String firstToken = tokens.get(0);
            log.info("🧪 단일 토큰 테스트: {}", firstToken);

            Message message = Message.builder()
                    .setToken(firstToken)
                    .setNotification(Notification.builder()
                            .setTitle("단일 테스트")
                            .setBody("Single message test")
                            .build())
                    .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("testSingleMessage 단일 메시지 전송 성공: {}", response);

        } catch (Exception e) {
            log.error("testSingleMessage 단일 메시지 실패: {}", e.getMessage());
        }
    }
}



