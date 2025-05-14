package Baeksa.money.domain.register.eventResponses;

import Baeksa.money.global.redis.eventResponses.OriginResponseEvent;

import java.util.Map;

public class RegisterApproveResponse extends OriginResponseEvent {
    public RegisterApproveResponse(OriginResponseEvent.Data data) {
        super("nestjs:response:membership:approve", data);
    }
}
