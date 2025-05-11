package Baeksa.money.domain.committee.event;

import lombok.Getter;

// 학생 거절 이벤트
@Getter
public class RejectStudentEvent extends BaseRedisEvent {
    public RejectStudentEvent(String channel, Object message) {
        super(channel, message);
    }
}
