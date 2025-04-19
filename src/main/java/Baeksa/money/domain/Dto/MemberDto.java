package Baeksa.money.domain.Dto;

import Baeksa.money.domain.enums.Role;
import Baeksa.money.global.excepction.BaseErrorCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String username;

//    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @NotBlank(message = "핸드폰 번호는 필수 입력 값입니다.")
    private String phoneNumber;

    private Long studentId;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
            message = "비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자의 비밀번호여야 합니다.")
    private String password;
    private String confirmPassword;

    private Role role;


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberResponseDto {
        private String username;
        private String email;
        private String phoneNumber;
        private Long studentId;
        private Role role; // enum 등
    }


    @Getter
    @Builder
    public static class ErrorResponse {

        private int status;
        private String code;
        private String message;

        public static ErrorResponse of(BaseErrorCode errorCode) {
            return ErrorResponse.builder()
                    .status(errorCode.getHttpStatus().value())
                    .code(errorCode.getCode())
                    .message(errorCode.getMessage())
                    .build();
        }
    }

    @Getter
    public static class TokenResponse {
        private final String username;
        private final Long studentId;
        private final String role;

        public TokenResponse(String username, Long studentId, String role) {
            this.username = username;
            this.studentId = studentId;
            this.role = role;
        }
    }

}
