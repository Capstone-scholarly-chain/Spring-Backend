package Baeksa.money.domain.auth.Entity;


import Baeksa.money.domain.auth.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="student_entity")
public class StudentEntity {

    @Id
    //학번은 고정, 학번을 id로 사용할 것이기에 아래 주석
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private String studentId;

    @Column(name = "username")
    private String username;

    @Column(name = "identity_number")
    private String identityNumber;  //주민번호

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

}
