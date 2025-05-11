package Baeksa.money.domain.committee.event;

import lombok.Getter;

@Getter
public class ApproveDepositEvent extends BaseRedisEvent {
    public ApproveDepositEvent(String channel, Object message) {
        super(channel, message);
    }
}
