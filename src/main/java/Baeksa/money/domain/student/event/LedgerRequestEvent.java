package Baeksa.money.domain.student.event;

import lombok.Getter;

// 원장 요청 이벤트
@Getter
public class LedgerRequestEvent extends BaseRedisEvent {
    public LedgerRequestEvent(String channel, Object message) {
        super(channel, message);
    }
}