package Baeksa.money.domain.ledger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Theme")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
//@EntityListeners(AuditingEntityListener.class)
@Builder
public class Theme {
    //연도랑 학기를 드롭다운 (상단에 필터링)
    //테마를 죽 나열
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "theme_name")
    private String themeName;

    @Column(name = "semester")
    private boolean semester;  //0->1 1->2

    @Column(name = "year")
    private int year;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
