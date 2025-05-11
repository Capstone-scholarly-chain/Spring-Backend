package Baeksa.money.domain.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class VoteDto {

    private final String entryId;


    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class setVodeDto{

        private String entryId;
        private int totalVotes;
        private int approvals;
        private int rejections;
        private String result;
        private Long processedAt;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThemeBalanceDto{

        private String theme;
        private Long totalDeposit;
        private Long totalWithdraw;
        private Long currentBalance;
        private int lastUpdated;
    }

}



