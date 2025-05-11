package Baeksa.money.domain.committee.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BaseRedisEvent {
    private final String channel;
    private final Object message;
}
