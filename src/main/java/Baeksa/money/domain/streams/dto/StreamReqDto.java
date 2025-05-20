package Baeksa.money.domain.streams.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
public class StreamReqDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamTestDto{
        private String message;
        private String requestId;
    }
}
