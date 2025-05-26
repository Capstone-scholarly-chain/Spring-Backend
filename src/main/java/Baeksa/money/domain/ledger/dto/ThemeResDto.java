package Baeksa.money.domain.ledger.dto;

import Baeksa.money.domain.ledger.Semester;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeResDto {

    private String themeName;
    private int year;
    private Semester semester;
    private LocalDateTime createdAt;
}
