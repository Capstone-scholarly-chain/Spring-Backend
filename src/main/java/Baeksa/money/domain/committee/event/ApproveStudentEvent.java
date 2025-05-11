package Baeksa.money.domain.committee.event;

import lombok.Getter;

@Getter
public class ApproveStudentEvent extends BaseRedisEvent {
    public ApproveStudentEvent(String channel, Object message) {
        super(channel, message);
    }
}