package Baeksa.money.domain.committee.event;

import lombok.Getter;

// 요청 ID 조회 이벤트 (필요한 경우)
@Getter
public class GetRequestEvent {
    private final String requestId;

    public GetRequestEvent(String requestId) {
        this.requestId = requestId;
    }
}
