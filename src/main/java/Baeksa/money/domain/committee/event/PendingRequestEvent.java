package Baeksa.money.domain.committee.event;

import lombok.Getter;

// 대기 중인 가입 요청 조회 이벤트
@Getter
public class PendingRequestEvent extends BaseRedisEvent {
    private final String requestId;

    public PendingRequestEvent(String channel, String requestId) {
        super(channel, "대기 중인 가입 요청 목록 요청:" + requestId);
        this.requestId = requestId;
    }
}