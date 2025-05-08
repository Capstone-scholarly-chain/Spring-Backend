package Baeksa.money.global.fcm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

@Builder
@AllArgsConstructor
@Getter
public class FcmMessage {

    private Message message;

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {
        private Notification notification;  //아래 알림 제목/본문/이미지url
        private String token;   //메세지를 보낼 대상 디바이스의 토큰
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Notification {
        private String title;
        private String body;
    }
}
