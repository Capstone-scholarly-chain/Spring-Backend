package Baeksa.money.domain.ledger.converter;


import Baeksa.money.domain.ledger.dto.ThemeReqDto;
import Baeksa.money.domain.ledger.dto.ThemeResDto;
import Baeksa.money.domain.ledger.entity.Theme;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ThemeConverter {

    public Theme convertDtoToTheme(ThemeReqDto.createThemeDto dto) {
        return Theme.builder()
                .themeName(dto.getThemeName())
                .year(dto.getYear())
                .semester(dto.getSemester())
                .build();
    }

    public ThemeResDto convertThemeToDto(Theme theme) {
        return ThemeResDto.builder()
                .themeName(theme.getThemeName())
                .year(theme.getYear())
                .semester(theme.getSemester())
                .createdAt(theme.getCreatedAt())
                .build();
    }

    public List<ThemeResDto> convertThemeToDtoList(List<Theme> themes){
        return themes.stream()
                .map(this::convertThemeToDto)
                .collect(Collectors.toList());
    }

}
