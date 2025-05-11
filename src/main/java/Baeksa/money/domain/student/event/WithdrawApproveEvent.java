package Baeksa.money.domain.student.event;

import lombok.Getter;

// 출금 승인 이벤트
@Getter
public class WithdrawApproveEvent extends BaseRedisEvent {
    public WithdrawApproveEvent(String channel, Object message) {
        super(channel, message);
    }
}
