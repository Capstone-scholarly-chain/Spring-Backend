package Baeksa.money.domain.committee.event;

import lombok.Getter;

// 학생 수 요청 이벤트
@Getter
public class StudentCountRequestEvent extends BaseRedisEvent {
    private final String requestId;

    public StudentCountRequestEvent(String channel, String requestId) {
        super(channel, "학생 조직원 수 요청:" + requestId);
        this.requestId = requestId;
    }
}
