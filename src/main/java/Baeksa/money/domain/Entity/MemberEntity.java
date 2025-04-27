package Baeksa.money.domain.Entity;


import Baeksa.money.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "member_entity")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    @Column(nullable = false)
    private String username;
    private String email;
    private String phoneNumber;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    //추후에 학생회가 학생을 조직원에 들일지 말지 투표 여부
//    @Enumerated(EnumType.STRING)
//    @Column(name = "active")
//    private Active active;


    public static MemberEntity fromToken(Long studentId, Role role) {
        MemberEntity entity = new MemberEntity();
        entity.studentId = studentId;
        entity.password = "ZoqtmxhsEmrkwk0228!"; // 사용 안 하는 placeholder
        entity.role = role;
        return entity;
    }

}
