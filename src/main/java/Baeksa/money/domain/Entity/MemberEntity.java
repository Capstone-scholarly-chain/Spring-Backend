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


//    @OneToOne
//    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
//    private StudentEntity studentId;  // ← 필드명은 유지!

//    @OneToOne
//    @JoinColumn(name = "student_id", referencedColumnName = "student_id", insertable = false, updatable = false)
//    private StudentEntity student;
//
//    private Long studentId; // student_id 필드를 따로 유지

    public static MemberEntity fromToken(String username, Long studentId, Role role) {
        MemberEntity entity = new MemberEntity();
        entity.username = username;
        entity.studentId = studentId;
        entity.password = "ZoqtmxhsEmrkwk0228!"; // 사용 안 하는 placeholder
        entity.role = role;
        return entity;
    }

}
