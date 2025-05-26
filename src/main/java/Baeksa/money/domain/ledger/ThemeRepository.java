package Baeksa.money.domain.ledger;

import Baeksa.money.domain.ledger.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    // 각 조건이 null이면 해당 조건은 무시
    @Query("SELECT t FROM Theme t WHERE " +
            "(:themeName IS NULL OR :themeName = '' OR t.themeName LIKE %:themeName%) AND " +
            "(:year IS NULL OR t.year = :year) AND " +
            "(:semester IS NULL OR t.semester = :semester)")
    List<Theme> findThemesByOptionalConditions(
            @Param("themeName") String themeName,
            @Param("year") Integer year,
            @Param("semester") Semester semester);

    Optional<Theme> findByThemeNameAndYearAndSemester(String themeName, int year, Semester semester);
    Optional<Theme> findByThemeName(String themeName);
}
