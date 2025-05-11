package Baeksa.money.domain.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TestDto {
    private String userId;
    private String requestId;
    private String theme;
    private String amount;
    private String description;
    private String documentURL;
}
