package Baeksa.money.global.fcm;

import lombok.Getter;

@Getter
public class FcmRequestDto {

    private String fcmToken;
    private String title;
    private String body;
}
