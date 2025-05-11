package Baeksa.money.global.excepction.code;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {


    @Schema(description = "HTTP 상태 코드", example = "400")
    private int status;

    @Schema(description = "에러 코드", example = "MEMBER_001")
    private String code;

    @Schema(description = "에러 메세지", example = "이미 가입된 학생입니다.")
    private String message;

    public static ErrorResponse of(BaseErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }
}