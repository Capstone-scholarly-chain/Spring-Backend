package Baeksa.money.domain.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ThemeReqDto {


    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class createThemeDto {
        private String themeName;
        private int year;
        private boolean semester;
        private LocalDateTime createdAt;

    }
}