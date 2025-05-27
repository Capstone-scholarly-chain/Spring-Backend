package Baeksa.money.domain.committee.controller;

import Baeksa.money.domain.committee.dto.CommitteeDto;
import Baeksa.money.domain.committee.service.CommitteeService;
import Baeksa.money.domain.streams.dto.StreamReqDto;
import Baeksa.money.domain.streams.service.RedisStreamProducer;
import Baeksa.money.domain.student.dto.StudentDto;
import Baeksa.money.global.config.swagger.ApiErrorCodeExample;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.jwt.CustomUserDetails;
import Baeksa.money.domain.committee.service.CommitteePublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

//EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "JWT_001", "refreshToken이 만료되었습니다."),
//INVALID_TOKEN(HttpStatus.BAD_REQUEST, "JWT_002", "refreshToken이 유효하지 않습니다."),
//TOKEN_NOTFOUND(HttpStatus.NOT_FOUND, "JWT_003", "refreshToken이 없습니다."),
//INVALID_ID(HttpStatus.BAD_REQUEST, "JWT_004", "학번이 잘못되었습니다."),
//BLACKLISTED(HttpStatus.BAD_REQUEST, "JWT_005", "blacklist처리된 access 토큰"),
//INVALID_ACCESS(HttpStatus.BAD_REQUEST, "JWT_006", "accessToken이 유효하지 않습니다."),
@Slf4j
@RestController
@RequestMapping("/api/committee")
@Tag(name = "학생회 회원 등록/장부 등록 API")
@RequiredArgsConstructor
public class CommitteeController {

    private final CommitteePublisher committeePublisher;
    private final CommitteeService committeeService;
    private final RedisStreamProducer redisStreamProducer;

//    @ApiErrorCodeExample(value = ErrorCode.class, include = {"INVALID_APPROVAL", "COMMITTEE_APPROVE"})
//    @PostMapping("/a")
//    public ResponseEntity<?> a(){
//        Map<String, String> map = new HashMap<>();
//        map.put("requestId", "바바바");
//        map.put("approverId", "dddddddd");
//        //로그인: 학생회, dto:학생회
//        //메시지를 담고, access되어있는 사람들한테 반복문으로 fcm알림을 날린다던지 하면 됨
//        committeePublisher.publish("spring:request:approve", map);
//        return ResponseEntity.ok(new BaseApiResponse<>(200, "PUBSUB-APPROVE", "학생회 가입 승인", map));
//    }

    //    @Operation(description = "학생회 가입 신청 - 학생회 화면") 없앰
// 학생회가 가입 신청하면 학생회가 승인/거절한다고..
    @Operation(summary = "학생회 가입 승인 - 학생회화면")
    @ApiErrorCodeExample(value = ErrorCode.class, include = {"INVALID_APPROVAL", "COMMITTEE_APPROVE"})
    @PatchMapping("/approve")
    public ResponseEntity<?> approve(@RequestBody CommitteeDto.approveDto approveDto,
                                     @AuthenticationPrincipal CustomUserDetails userDetails){
        //로그인: 학생회, dto:학생회
        //메시지를 담고, access되어있는 사람들한테 반복문으로 fcm알림을 날린다던지 하면 됨
//        Map<String, Object> result = committeePublisher.approveStudent(userDetails.getStudentId(), streamApproveDto);
        RecordId recordId = redisStreamProducer.sendMessage(
                new StreamReqDto.streamApproveDto(userDetails.getStudentId(), approveDto.getRequestId()), "APPROVE_MEMBERSHIP");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "APPROVE_MEMBERSHIP", "학생회가 학생 가입 승인", recordId));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"INVALID_APPROVAL", "COMMITTEE_REJECT"})
    @Operation(summary = "학생회 가입 거절 - 학생회화면")
    @PatchMapping("/reject")
    public ResponseEntity<?> reject(@RequestBody CommitteeDto.rejectDto rejectDto,
                                    @AuthenticationPrincipal CustomUserDetails userDetails){
        //로그인: 학생회, dto:학생회, 타임스탬프 아예 뺌
//        Map<String, Object> result = committeePublisher.rejectStudent(userDetails.getStudentId(), streamRejectDto);
        RecordId recordId = redisStreamProducer.sendMessage(
                new StreamReqDto.streamRejectDto(userDetails.getStudentId(), rejectDto.getRequestId()), "REJECT_MEMBERSHIP");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "REJECT_MEMBERSHIP", "학생회가 학생 가입 거절", recordId));
    }


    @ApiErrorCodeExample(value = ErrorCode.class, include = {"STUDENT_NOTFOUND", "COMMITTEE_WITHDRAW"})
    @Operation(summary = "학생회 출금 기입 신청 - 학생회 화면")
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody CommitteeDto.LedgerDto ledgerDto) {

//        Map<String, Object> result = committeePublisher.applyWithdraw(userDetails.getStudentId(), ledgerDto);
        RecordId recordId = redisStreamProducer.sendMessage(
                new StreamReqDto.streamLedgerDto(userDetails.getStudentId(), ledgerDto.getTheme(), ledgerDto.getAmount(),
                        ledgerDto.getDescription(), ledgerDto.getDocumentURL()), "COMMITTEE_APPLY_WITHDRAW");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "COMMITTEE_APPLY_WITHDRAW", "학생회 출금 기입 요청", recordId));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"COMMITTEE_APPROVE_DEPOSIT"})
    @Operation(summary = "학생회 입금 승인 - 학생회 화면")
    @PostMapping("/approve-deposit")
    public ResponseEntity<?> deposit(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestBody StudentDto.ledgerApproveDto dto){
        //학생회와 학생을 받아...
//        Map<String, Object> map = committeePublisher.approveDeposit(userDetails.getStudentId(), dto.getLedgerEntryId());
        RecordId recordId = redisStreamProducer.sendMessage(
                new StreamReqDto.streamLedgerApproveDto(userDetails.getStudentId(), dto.getLedgerEntryId()),
                "COMMITTEE_APPROVE_DEPOSIT");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "COMMITTEE_APPROVE_DEPOSIT", "학생회가 입금 내역 승인", recordId));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"COMMITTEE_REJECT_DEPOSIT"})
    @Operation(summary = "학생회 입금 거절 - 학생회 화면")
    @PostMapping("/reject-deposit")
    public ResponseEntity<?> rejectDeposit(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody StudentDto.ledgerRejectDto dto){
        //로그인: 학생회, body: 신청한 학생
//        Map<String, Object> map = committeePublisher.rejectDeposit(userDetails.getStudentId(), dto.getLedgerEntryId());
        RecordId recordId = redisStreamProducer.sendMessage(
                new StreamReqDto.streamLedgerRejectDto(userDetails.getStudentId(), dto.getLedgerEntryId()),
                "COMMITTEE_REJECT_DEPOSIT");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "COMMITTEE_REJECT_DEPOSIT", "학생회가 입금 내역 거절", recordId));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {""})
    @Operation(summary = "학생회 수 조회")
    @GetMapping("/counts")
    public ResponseEntity<?> getCommitteeCount(){

        String count = committeeService.findCommitteeCount();
        int counts = Integer.parseInt(count);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "FIND-COMMITTEE-COUNTS", "모든 학생회 수 조회", counts));
    }
}

