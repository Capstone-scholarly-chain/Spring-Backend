package Baeksa.money.domain.ledger.entity;

import Baeksa.money.domain.ledger.Semester;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "theme")  //이것도 소문자로 해라..
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Theme {
    //연도랑 학기를 드롭다운 (상단에 필터링)
    //테마를 죽 나열
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "theme_name")
    private String themeName;

//    @Column(name = "semester")
//    private boolean semester;  //0->1 1->2
    @Column(name = "semester")
    private Semester semester;

    @Column(name = "year")
    private int year;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
