package Baeksa.money.domain.auth.Dto;

import Baeksa.money.domain.auth.enums.Role;
import Baeksa.money.domain.auth.enums.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String username;

    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @NotBlank(message = "핸드폰 번호는 필수 입력 값입니다.")
    private String phoneNumber;

    private String studentId;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
            message = "비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자의 비밀번호여야 합니다.")
    private String password;
    private String confirmPassword;

    private Role role;
    private LocalDateTime timestamp;
    private LocalDateTime updateAt;
    private Status status;
    private List<String> approvals;
    private List<String> rejections;
    private Long id;




    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberResponseDto {
        private String username;
        private String email;
        private String phoneNumber;
        private String studentId;
        private Role role; // enum 등
        private Status status;
        private LocalDateTime timestamp;
        private LocalDateTime updateAt;
    }


}
