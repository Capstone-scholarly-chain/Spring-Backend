package Baeksa.money.domain.student.dto;

import Baeksa.money.domain.ledger.enums.Semester;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {

    private String studentId;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LedgerReqDto{
        private String theme;           //name_year_semester
        private int year;
        private Semester semester;
        private Long amount;
        private String description;
        private String documentURL;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VoteDto {
        private String voterId;
        private boolean vote;
        private String ledgerEntryId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ledgerApproveDto {
        private String ledgerEntryId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ledgerRejectDto {
        private String ledgerEntryId;
    }
}
