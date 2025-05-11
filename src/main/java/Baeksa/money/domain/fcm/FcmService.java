//package Baeksa.money.domain.fcm;
//
//import com.google.firebase.messaging.*;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Collections;
//import java.util.List;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class FcmService {
//
//    private final FcmTokenRepository fcmTokenRepository;
//
////    @Transactional
////    public void saveFcmToken(Long studentId, String fcmToken) {
////        fcmTokenRepository.save(new FcmToken(studentId, fcmToken));
////    }
//
//    @Transactional
//    public void addFcmToken(Long studentId, String newToken) {
//        FcmToken fcmToken = fcmTokenRepository.findById(studentId).orElse(new FcmToken(studentId));
//
//        if (!fcmToken.getFcmTokens().contains(newToken)) {
//            fcmToken.getFcmTokens().add(newToken);
//            fcmTokenRepository.save(fcmToken);
//        }
//    }
//
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
//
////    public void sendMessage(String fcmToken, String title, String body)
////            throws FirebaseMessagingException {
////
////        // 메시지 생성
////        Notification notification = Notification.builder()
////                .setTitle(title)
////                .setBody(body)
////                .build();
////
////        Message message = Message.builder()
////                .setToken(fcmToken)  // FCM Token 사용
////                .setNotification(notification)
////                .build();
////
////        String response = FirebaseMessaging.getInstance().send(message);
////        log.info("FCM 전송 성공: {}", response);
////    }
//}
