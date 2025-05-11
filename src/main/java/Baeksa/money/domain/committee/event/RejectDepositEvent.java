package Baeksa.money.domain.committee.event;

import lombok.Getter;

// 입금 거절 이벤트
@Getter
public class RejectDepositEvent extends BaseRedisEvent {
    public RejectDepositEvent(String channel, Object message) {
        super(channel, message);
    }
}
