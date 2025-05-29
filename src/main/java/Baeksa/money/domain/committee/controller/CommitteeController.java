package Baeksa.money.domain.committee.controller;

import Baeksa.money.domain.committee.dto.CommitteeDto;
import Baeksa.money.domain.committee.service.CommitteeService;
import Baeksa.money.domain.s3.service.S3Service;
import Baeksa.money.domain.streams.dto.StreamReqDto;
import Baeksa.money.domain.streams.service.RedisStreamProducer;
import Baeksa.money.domain.student.dto.StudentDto;
import Baeksa.money.global.config.swagger.ApiErrorCodeExample;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.jwt.CustomUserDetails;
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

    private final CommitteeService committeeService;
    private final RedisStreamProducer redisStreamProducer;
    private final S3Service s3Service;

    //    @Operation(description = "학생회 가입 신청 - 학생회 화면") 없앰
// 학생회가 가입 신청하면 학생회가 승인/거절한다고..
    @Operation(summary = "가입 승인 - 학생회화면", description = "학생/학생회가 가입 신청하면 학생회 1명이 승인 '\' " +
            "REQ_STUDENT_TIMESTAMP_학번 '\' " + "REQ_COUNCIL_TIMESTAMP_학번")
    @ApiErrorCodeExample(value = ErrorCode.class, include = {"INVALID_APPROVAL", "COMMITTEE_APPROVE"})
    @PatchMapping("/approve")
    public ResponseEntity<?> approve(@RequestBody CommitteeDto.approveDto approveDto,
                                     @AuthenticationPrincipal CustomUserDetails userDetails){
        RecordId recordId = redisStreamProducer.sendMessage(
                new StreamReqDto.streamApproveDto(userDetails.getStudentId(), userDetails.getRealUsername(), approveDto.getRequestId()),
                "APPROVE_MEMBERSHIP");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "APPROVE_MEMBERSHIP", "학생회가 학생 가입 승인", recordId));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"INVALID_APPROVAL", "COMMITTEE_REJECT"})
    @Operation(summary = "가입 거절 - 학생회화면", description = "학생/학생회가 가입 신청하면 학생회 1명이 거절 '\' " +
            "REQ_STUDENT_TIMESTAMP_학번 '\' " + "REQ_COUNCIL_TIMESTAMP_학번")
    @PatchMapping("/reject")
    public ResponseEntity<?> reject(@RequestBody CommitteeDto.rejectDto rejectDto,
                                    @AuthenticationPrincipal CustomUserDetails userDetails){
        RecordId recordId = redisStreamProducer.sendMessage(
                new StreamReqDto.streamRejectDto(userDetails.getStudentId(), userDetails.getRealUsername(), rejectDto.getRequestId()),
                "REJECT_MEMBERSHIP");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "REJECT_MEMBERSHIP", "학생회가 학생 가입 거절", recordId));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"COMMITTEE_APPROVE_DEPOSIT"})
    @Operation(summary = "입금 승인 - 학생회 화면", description = "학생이 신청한 입금에 대한 학생회 1명이 승인")
    @PostMapping("/approve-deposit")
    public ResponseEntity<?> deposit(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestBody StudentDto.ledgerApproveDto dto){
        RecordId recordId = redisStreamProducer.sendMessage(
                new StreamReqDto.streamLedgerApproveDto(userDetails.getStudentId(), dto.getLedgerEntryId()),
                "APPROVE_DEPOSIT");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "APPROVE_DEPOSIT", "학생회가 입금 내역 승인", recordId));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"COMMITTEE_REJECT_DEPOSIT"})
    @Operation(summary = "입금 거절 - 학생회 화면", description = "학생이 신청한 입금에 대한 학생회 1명이 거절")
    @PostMapping("/reject-deposit")
    public ResponseEntity<?> rejectDeposit(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody StudentDto.ledgerRejectDto dto){
        //로그인: 학생회, body: 신청한 학생
        RecordId recordId = redisStreamProducer.sendMessage(
                new StreamReqDto.streamLedgerRejectDto(userDetails.getStudentId(), dto.getLedgerEntryId()),
                "REJECT_DEPOSIT");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "REJECT_DEPOSIT", "학생회가 입금 내역 거절", recordId));
    }


    @ApiErrorCodeExample(value = ErrorCode.class, include = {"STUDENT_NOTFOUND", "COMMITTEE_WITHDRAW"})
    @Operation(summary = "학생회 출금 기입 신청 - 학생회 화면")
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody CommitteeDto.LedgerDto ledgerDto) {

        // ledgerDto.getDocumentURL()에는 이미 업로드 완료된 S3 파일 URL이 들어있음
        // 내가 하는게 아니다
        RecordId recordId = redisStreamProducer.sendMessage(
                new StreamReqDto.streamLedgerDto(userDetails.getStudentId(), ledgerDto.getTheme(), ledgerDto.getAmount(),
                        ledgerDto.getDescription(), ledgerDto.getDocumentURL()), "ADD_WITHDRAW");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "ADD_WITHDRAW", "학생회 출금 기입 요청", recordId));
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

