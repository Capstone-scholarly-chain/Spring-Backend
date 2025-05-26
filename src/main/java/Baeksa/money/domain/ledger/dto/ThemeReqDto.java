package Baeksa.money.domain.ledger.dto;

import Baeksa.money.domain.ledger.Semester;
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
        private Semester semester;
    }
}