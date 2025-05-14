package Baeksa.money.global.redis.eventResponses;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Getter
public class GetResponseEvent {
    private String channel;
    private String requestId;

    public GetResponseEvent(String channel, String requestId) {
        this.channel = channel;
        this.requestId = requestId;
    }
}
