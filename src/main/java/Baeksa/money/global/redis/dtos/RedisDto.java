package Baeksa.money.global.redis.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true) //메세지에 정의하지 않은 필드가 있어도 무시하고 파싱
public class RedisDto {

    @Getter
    @AllArgsConstructor
    public static class TokenResponse {
        private final String studentId;
        private final String role;
        private final String username;
        private final String status;
    }

    @Getter
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class Refresh{

        @JsonProperty("refresh_token")
        private final String refresh_token;
    }
}
