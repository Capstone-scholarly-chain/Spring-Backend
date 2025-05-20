package Baeksa.money.domain.streams.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class StreamReqDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamTestDto{
        private String message;
        private String requestId;
    }
}
