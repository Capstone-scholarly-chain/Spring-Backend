package Baeksa.money.domain.student.controller;


import Baeksa.money.domain.auth.anotation.ApprovedOnly;
import Baeksa.money.domain.ledger.service.ThemeService;
import Baeksa.money.domain.streams.dto.StreamReqDto;
import Baeksa.money.domain.streams.service.RedisStreamProducer;
import Baeksa.money.domain.student.dto.StudentDto;
import Baeksa.money.domain.student.service.StudentService;
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

@Slf4j
@RestController
@RequestMapping("/api/student")
@Tag(name = "학생 회원 등록/장부 등록 API")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final RedisStreamProducer redisStreamProducer;

    @Operation(summary = "학생이 입금 기입 요청을 보냄 - 학생 화면")
    @ApiErrorCodeExample(value = ErrorCode.class, include = {"STUDENT_NOTFOUND", "STUDENT_APPLY_LEDGER"})
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestBody StudentDto.LedgerReqDto ledgerDto){

        String themeId = ledgerDto.getTheme() + "_" + ledgerDto.getYear() + "_" + ledgerDto.getSemester();
        RecordId recordId = redisStreamProducer.sendMessage(new StreamReqDto.streamLedgerDto(userDetails.getStudentId(), themeId,
                ledgerDto.getAmount(), ledgerDto.getDescription(), ledgerDto.getDocumentURL()), "ADD_DEPOSIT");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "ADD_DEPOSIT", "학생 입금 기입 요청", recordId));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"STUDENT_APPROVE_WITHDRAW"})
    @Operation(summary = "학생 출금 승인 - 학생 화면")
    @ApprovedOnly
    @PostMapping("vote-withdraw")
    public ResponseEntity<?> voteWithdraw(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestBody StudentDto.VoteDto dto){
        // 투표한 사람, 찬성/반대, 출금 승인 구분할 수 있는 LedgerEntryId
        // 투표한 사람 넣었는지? -> 안 넣음...
        RecordId recordId = redisStreamProducer.sendMessage(
                new StudentDto.VoteDto(userDetails.getStudentId(), dto.isVote(), dto.getLedgerEntryId()),
                "VOTE_WITHDRAW");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "VOTE_WITHDRAW", "출금 기입 투표 승인/거절", recordId));
    }

//    @ApprovedOnly 나중에 넣기
    @ApiErrorCodeExample(value = ErrorCode.class, include = {""})
    @Operation(summary = "학생 수 조회")
    @GetMapping("/counts")
    public ResponseEntity<?> getStudentCount(){

        String count = studentService.findStudentCount();
        int counts = Integer.parseInt(count);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "FIND-STUDENTS-COUNTS", "모든 학생 수 조회", counts));
    }
}