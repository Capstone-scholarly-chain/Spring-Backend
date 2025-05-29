package Baeksa.money.domain.ledger.dto;

import Baeksa.money.domain.ledger.enums.Semester;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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