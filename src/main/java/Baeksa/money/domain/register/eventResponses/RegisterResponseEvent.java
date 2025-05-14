package Baeksa.money.domain.register.eventResponses;

import Baeksa.money.global.redis.eventResponses.OriginResponseEvent;

import java.util.Map;

public class RegisterResponseEvent extends OriginResponseEvent {
    public RegisterResponseEvent(OriginResponseEvent.Data data) {
        super("nestjs:response:register-user", data);
    }
}
