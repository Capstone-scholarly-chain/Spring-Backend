package Baeksa.money.domain.committee.event;

import lombok.Getter;

// Redis 응답 이벤트
@Getter
public class RedisResponseEvent {
    private final String channel;
    private final String requestId;

    public RedisResponseEvent(String channel, String requestId) {
        this.channel = channel;
        this.requestId = requestId;
    }
}
