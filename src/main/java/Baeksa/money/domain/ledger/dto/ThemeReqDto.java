package Baeksa.money.domain.ledger.dto;

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
        private boolean semester;
        private int year;
    }
}