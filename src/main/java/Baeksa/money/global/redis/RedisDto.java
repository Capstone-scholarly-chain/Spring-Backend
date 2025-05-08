package Baeksa.money.global.redis;

import Baeksa.money.domain.enums.EntryType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) //메세지에 정의하지 않은 필드가 있어도 무시하고 파싱
public class RedisDto {

    private boolean agree;
    private Long studentId;
    private String reason;

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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class MessageDto{
        private String channel;
        private Map<String, Object> message;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class pubDto{
        private boolean active;
        private Long applicantId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LedgerDto{
        private String theme;
        private Long amount;
        private String description;
        private EntryType entryType;
    }

//    @Getter
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class LedgerRequestDto{
//        private Long ledgerEntryId;
//    }
}
