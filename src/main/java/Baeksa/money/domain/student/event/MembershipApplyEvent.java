package Baeksa.money.domain.student.event;

import lombok.Getter;

// 회원가입 신청 이벤트
@Getter
public class MembershipApplyEvent extends BaseRedisEvent {
    public MembershipApplyEvent(String channel, Object message) {
        super(channel, message);
    }
}