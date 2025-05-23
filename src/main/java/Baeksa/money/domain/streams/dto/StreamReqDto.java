package Baeksa.money.domain.streams.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
public class StreamReqDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamTestDto{
        private String userId;
        private String theme;
        private String amount;
        private String description;
        private String documentURL;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterUserDto{
        private String userId;
        private String name;
        private String orgType;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipApprovalDto{
        private String requestId;
        private String approverId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipRejectionDto{
        private String requestId;
        private String rejectorId;
    }

    //조회 라인
    //    'GET_STUDENT_COUNT': null,
    //    'GET_COUNCIL_COUNT': null,
    //    'GET_PENDING_REQUESTS': null,

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetOneRegisterStatus{
        private String requestId;
    }


    //success 여부가 포함된 기본 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuccessResponse{
        private String originalRecordId;
        private String requestType;
        private String success;
        private String result;
        private String processingTime;
        private String timestamp;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse{
        private String originalRecordId;
        private String requestType;
        private String success;
        private String error;
        private String processingTime;
        private String timestamp;
    }
}
