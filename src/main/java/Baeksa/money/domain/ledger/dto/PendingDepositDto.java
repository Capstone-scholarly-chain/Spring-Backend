package Baeksa.money.domain.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingDepositDto {
    private String id;
    private String theme;
    private double amount;
    private String entryType;
    private String status;
    private String approvedBy;
    private String createdBy;
    private String creatorName;
    private long timestamp;
    private long expiryTime;
    private String description;
    private List<String> approvals;
    private List<String> rejections;
    private String documentURL;
}
