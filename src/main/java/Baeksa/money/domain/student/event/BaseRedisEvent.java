package Baeksa.money.domain.student.event;

import lombok.Getter;

@Getter
public abstract class BaseRedisEvent {
    private final String channel;
    private final Object message;

    protected BaseRedisEvent(String channel, Object message) {
        this.channel = channel;
        this.message = message;
    }
}




