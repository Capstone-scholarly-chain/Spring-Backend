package Baeksa.money.domain.ledger.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeResDto {

    private String themeName;
    private int year;
    private boolean semester;
    private LocalDateTime createdAt;
}
