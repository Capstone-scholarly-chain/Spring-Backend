package Baeksa.money.domain.committee.event;

import lombok.Getter;

// 사용자 가입 상태 조회 이벤트
@Getter
public class UserStatusRequestEvent extends BaseRedisEvent {
    private final String requestId;
    private final String userRequestId;

    public UserStatusRequestEvent(String channel, String requestId, String userRequestId) {
        super(channel, null); // 메시지는 JSON으로 별도 처리됨
        this.requestId = requestId;
        this.userRequestId = userRequestId;
    }
}
