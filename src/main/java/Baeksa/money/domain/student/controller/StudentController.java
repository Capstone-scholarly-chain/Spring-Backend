package Baeksa.money.domain.student.controller;


import Baeksa.money.domain.student.dto.StudentDto;
import Baeksa.money.domain.student.service.StudentPublisher;
import Baeksa.money.domain.student.service.StudentService;
import Baeksa.money.global.config.swagger.ApiErrorCodeExample;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pubsub/student")
@Tag(name = "학생 회원 등록/장부 등록 API")
@RequiredArgsConstructor
public class StudentController {

    private final StudentPublisher studentPublisher;
    private final StudentService studentService;

//    @ApiErrorCodeExample(value = ErrorCode.class, include = {"STUDENT_APPLY"})
//    @Operation(summary = "학생 가입 신청 - 학생 화면")
//    @PostMapping("/membership")
//    public ResponseEntity<?> membership(@AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        Map<String, Object> map = studentPublisher.apply(userDetails.getStudentId());
////                fcmService.sendMessage(fcmToken, "학생 가입 신청", dto.getUsername() + "이 가입 신청했습니다.");
//        return ResponseEntity.ok(new BaseApiResponse<>(200, "PUBSUB-MEMBERSHIP", "학생 가입 신청", map));
//    }

    //프론트는 Theme, amount, description, documentURL만 넘겨주면 됨
    @Operation(summary = "학생이 입금 기입 요청을 보냄 - 학생 화면")
    @ApiErrorCodeExample(value = ErrorCode.class, include = {"STUDENT_NOTFOUND", "STUDENT_APPLY_LEDGER"})
    @PostMapping("/ledger")
    public ResponseEntity<?> ledger(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestBody StudentDto.LedgerDto ledgerDto){

        Map<String, Object> map = studentPublisher.applyLedger(userDetails.getStudentId(), ledgerDto);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "STUDENT_APPLY_LEDGER", "학생 입금 기입 요청", map));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"STUDENT_APPROVE_WITHDRAW"})
    @Operation(summary = "학생 출금 승인 - 학생 화면")
    @PostMapping("vote-withdraw")
    public ResponseEntity<?> approve(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestBody StudentDto.VoteDto dto){

        Map<String, Object> map = studentPublisher.voteWithdraw(userDetails.getStudentId(), dto);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "STUDENT_VOTE_WITHDRAW", "출금 기입 투표 승인/거절", map));
    }

//    @ApiErrorCodeExample(value = ErrorCode.class, include = {"STUDENT_REJECT_WITHDRAW"})
//    @Operation(summary = "학생 출금 거부 - 학생 화면")
//    @PostMapping("reject-withdraw")
//    public ResponseEntity<?> reject(@AuthenticationPrincipal CustomUserDetails userDetails,
//                                    @RequestBody StudentDto.VoteDto dto){
//
//           Map<String, Object> map = studentPublisher.voteWithdraw(userDetails.getStudentId(), dto);
//        return ResponseEntity.ok(new BaseApiResponse<>(200, "PUBSUB-REJECT-WITHDRAW", "출금 기입 거절", map));
//    }


    @ApiErrorCodeExample(value = ErrorCode.class, include = {""})
    @Operation(summary = "학생 수 조회")
    @GetMapping("/counts")
    public ResponseEntity<?> getStudentCount(){

        int count = studentService.findStudentCount();
        return ResponseEntity.ok(new BaseApiResponse<>(200, "FIND-STUDENTS-COUNTS", "모든 학생 수 조회", count));
    }
}