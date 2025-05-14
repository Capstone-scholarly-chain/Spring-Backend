package Baeksa.money.domain.register.eventResponses;

import Baeksa.money.global.redis.eventResponses.OriginResponseEvent;

import java.util.Map;

public class RegisterRejectResponse extends OriginResponseEvent {
    public RegisterRejectResponse(OriginResponseEvent.Data data) {
        super("nestjs:response:membership:rejected", data);
    }
}
