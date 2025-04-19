package Baeksa.money.domain.Dto;

import Baeksa.money.domain.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDto {

    private String username;
    private Long studentId;
    private String identityNumber;
    private String phoneNumber;
    private Role role;

}
