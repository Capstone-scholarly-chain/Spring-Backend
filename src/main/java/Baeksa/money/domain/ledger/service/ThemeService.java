package Baeksa.money.domain.ledger.service;

import Baeksa.money.domain.ledger.ThemeConverter;
import Baeksa.money.domain.ledger.ThemeRepository;
import Baeksa.money.domain.ledger.dto.ThemeReqDto;
import Baeksa.money.domain.ledger.dto.ThemeResDto;
import Baeksa.money.domain.ledger.entity.Theme;
import Baeksa.money.global.excepction.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import Baeksa.money.global.excepction.code.ErrorCode;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ThemeConverter themeConverter;

    public ThemeResDto create(ThemeReqDto.createThemeDto createThemeDto) {

        Theme theme = themeRepository.findByThemeName(createThemeDto.getThemeName());
        if(theme.getYear() == createThemeDto.getYear() && theme.isSemester() == createThemeDto.isSemester()) {
            throw new CustomException(ErrorCode.DUPLICATED_THEME);
        }
        Theme savedTheme = themeConverter.convertDtoToTheme(createThemeDto);
        themeRepository.save(savedTheme);

        ThemeResDto themeResDto = themeConverter.convertThemeToDto(savedTheme);
        return themeResDto;
    }

    public List<ThemeResDto> searchThemes(ThemeReqDto.createThemeDto dto) {
        try {
            // DTO에서 값 추출 (null 체크 포함)
            String themeName = (dto.getThemeName() != null && !dto.getThemeName().trim().isEmpty())
                    ? dto.getThemeName().trim() : null;
            Integer year = (dto.getYear() > 0) ? dto.getYear() : null;
            Boolean semester = dto.isSemester(); // boolean이므로 null 체크는 Boolean 타입으로 변경 필요

            log.info("테마 검색 조건: themeName={}, year={}, semester={}", themeName, year, semester);

            List<Theme> results = themeRepository.findThemesByOptionalConditions(themeName, year, semester);
            log.info("테마 검색 결과: {}개 발견", results.size());

            List<ThemeResDto> themeResDtoList = themeConverter.convertThemeToDtoList(results);
            return themeResDtoList;

        } catch (Exception e) {
            log.error("테마 검색 실패: error={}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
