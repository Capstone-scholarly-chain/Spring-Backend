package Baeksa.money.domain.student.event;

import lombok.Getter;

import java.util.Map;

// Redis 응답 이벤트
@Getter
public class RedisResponseEvent {
    private final String channel;
    private final Map<String, Object> message;

    public RedisResponseEvent(String channel, Map<String, Object> message) {
        this.channel = channel;
        this.message = message;
    }
}