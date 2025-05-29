package Baeksa.money.domain.ledger.service;

import Baeksa.money.domain.ledger.enums.Semester;
import Baeksa.money.domain.ledger.converter.ThemeConverter;
import Baeksa.money.domain.ledger.repo.ThemeRepository;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ThemeConverter themeConverter;

    public ThemeResDto create(ThemeReqDto.createThemeDto createThemeDto) {
        Optional<Theme> themeOpt = themeRepository.findByThemeNameAndYearAndSemester(
                createThemeDto.getThemeName(),
                createThemeDto.getYear(),
                createThemeDto.getSemester());

        if (themeOpt.isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_THEME);
        }

        // 새로운 테마 생성
        Theme savedTheme = themeConverter.convertDtoToTheme(createThemeDto);
        themeRepository.save(savedTheme);

        return themeConverter.convertThemeToDto(savedTheme);
    }

    public List<ThemeResDto> searchThemes(String themeName, Integer year, Semester semester) {
        try {
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
