package Baeksa.money.domain.committee.event;

import lombok.Getter;

// 출금 요청 이벤트
@Getter
public class WithdrawRequestEvent extends BaseRedisEvent {
    public WithdrawRequestEvent(String channel, Object message) {
        super(channel, message);
    }
}
