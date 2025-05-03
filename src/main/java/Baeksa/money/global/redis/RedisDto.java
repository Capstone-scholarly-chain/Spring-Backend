package Baeksa.money.global.redis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class RedisDto {

    @Getter
    @AllArgsConstructor
    public static class TokenResponse {
        private final Long studentId;
        private final String role;
    }


    @Getter
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class Refresh{

        @JsonProperty("refresh")
        private final String refresh;
    }

    @Getter
    @AllArgsConstructor
    public static class MessageDto{
        private String channel;
        private String message;
        private String sender;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class pubDto{
        private boolean active;
        private Long applicantId;
    }
}
