package Baeksa.money.global.redis.eventResponses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Redis 응답 이벤트 기본 클래스
 */

@NoArgsConstructor
@Getter
public class OriginResponseEvent {
    private String channel;
    private Data data;

    public OriginResponseEvent(String channel, Data data) {
        this.channel = channel;
        this.data = data;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Data {
        private String id;  //requestId
        private String applicant;
        private String orgType;
        private String status;
        private String[] approvals;
        private String[] rejections;
        private String timestamp;
        private String expiryTime;
    }
}

