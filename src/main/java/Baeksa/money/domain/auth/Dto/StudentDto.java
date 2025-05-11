package Baeksa.money.domain.auth.Dto;

import Baeksa.money.domain.auth.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDto {

    private String username;
    private String studentId;
    private String identityNumber;
    private String phoneNumber;
    private Role role;

}
