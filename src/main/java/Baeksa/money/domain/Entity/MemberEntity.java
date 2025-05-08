package Baeksa.money.domain.Entity;


import Baeksa.money.domain.enums.Role;
import Baeksa.money.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.security.Timestamp;
import java.time.LocalDateTime;


@Entity
@Table(name = "member_entity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
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
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @CreatedDate
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @LastModifiedDate
    @Column(name = "update_at")
    private LocalDateTime updateAt;


    public static MemberEntity fromToken(Long studentId, Role role) {
        MemberEntity entity = new MemberEntity();
        entity.studentId = studentId;
        entity.password = "ZoqtmxhsEmrkwk0228!"; // 사용 안 하는 placeholder
        entity.role = role;
        return entity;
    }



    //조직원 들일지 말지 상태 변경
    public void approve(){
        this.status = Status.APPROVE;
    }
    public void reject(){
        this.status = Status.REJECT;
    }

}
