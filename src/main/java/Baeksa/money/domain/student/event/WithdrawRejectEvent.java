package Baeksa.money.domain.student.event;

import lombok.Getter;

// 출금 거절 이벤트
@Getter
public class WithdrawRejectEvent extends BaseRedisEvent {
    public WithdrawRejectEvent(String channel, Object message) {
        super(channel, message);
    }
}

