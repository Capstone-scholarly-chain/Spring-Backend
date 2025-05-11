package Baeksa.money.domain.committee.event;

import lombok.Getter;

// 학생회 수 요청 이벤트
@Getter
public class CommitteeCountRequestEvent extends BaseRedisEvent {
    private final String requestId;

    public CommitteeCountRequestEvent(String channel, String requestId) {
        super(channel, "학생회 조직원 수 요청:" + requestId);
        this.requestId = requestId;
    }
}
