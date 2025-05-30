package Baeksa.money.domain.ledger.controller;

import Baeksa.money.domain.auth.anotation.ApprovedOnly;
import Baeksa.money.domain.ledger.enums.Semester;
import Baeksa.money.domain.ledger.dto.ThemeReqDto;
import Baeksa.money.domain.ledger.dto.ThemeResDto;
import Baeksa.money.domain.ledger.service.LedgerService;
import Baeksa.money.domain.ledger.dto.VoteDto;
import Baeksa.money.domain.ledger.service.ThemeService;
import Baeksa.money.domain.streams.service.RedisStreamProducer;
import Baeksa.money.global.config.swagger.ApiErrorCodeExample;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import Baeksa.money.global.excepction.code.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/ledger")
@RestController
@RequiredArgsConstructor
@Tag(name = "Ledger관련 api")
public class LedgerController {

    // 조회 라인은 stream을 서비스 안에서 함
    private final LedgerService ledgerService;
    private final ThemeService themeService;
    private final RedisStreamProducer redisStreamProducer;

    @Operation(summary = "테마 생성")
    @ApprovedOnly
    @PostMapping("/create-theme")
    public ResponseEntity<?> theme(@RequestBody ThemeReqDto.createThemeDto dto){

        ThemeResDto themeResDto = themeService.create(dto);
        return ResponseEntity.ok(new BaseApiResponse<>(201, "CREATE_THEME",
                "테마 생성 완료", themeResDto));
    }

    @Operation(summary = "테마 조회")
    @ApprovedOnly
    @GetMapping("/get-theme")
    public ResponseEntity<?> getTheme(@RequestParam(required = false) String themeName,
                                      @RequestParam(required = false) Integer year,
                                      @RequestParam(required = false) Semester semester){

        List<ThemeResDto> themeResDtoList = themeService.searchThemes(themeName, year, semester);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "GET_THEMES",
                "테마 조회", themeResDtoList));
    }

/////////////////////////////////////////////

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"PENDING_DEPOSIT_FETCH_FAILED"})
    @Operation(summary = "대기중인 입금 항목 조회")
    @ApprovedOnly
    @GetMapping("/deposits")
    public ResponseEntity<?> deposits(){
        List<Map<String, Object>> pendingDeposits = ledgerService.getPendingDeposits();
        return ResponseEntity.ok(new BaseApiResponse<>(200, "GET-DEPOSITS",
                "대기중인 입금 항목 조회", pendingDeposits));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"PENDING_WITHDRAW_FETCH_FAILED"})
    @Operation(summary = "대기중인 출금 항목 조회")
    @ApprovedOnly
    @GetMapping("/withdrawals")
    public ResponseEntity<?> withdrawals(){
        List<Map<String, Object>> pendingWithdrawals = ledgerService.getPendingWithdrawals();
        return ResponseEntity.ok(new BaseApiResponse<>(200, "GET-WITHDRAWALS",
                "대기중인 출금 항목 조회", pendingWithdrawals));
    }


//    @GetMapping("/{studentId}")
//    public ResponseEntity<?> getMyList(@PathVariable("studentId") Long studentId){
//        List<String> myList = ledgerService.getMyList(studentId);
//        return ResponseEntity.ok(new BaseApiResponse<>(200, "GET-MYLIST",
//                "나의 내역 조회", myList));
//    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "MY_HISTORY_FETCH_FAILED"})
    @Operation(summary = "나의 내역 조회(완료)")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getMyList(@PathVariable("userId") String userId){
//        RecordId recordId = redisStreamProducer.sendMessage(userId, "GET_MY_LEDGER");
        List<Map<String, Object>> myList = ledgerService.getMyList(userId);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "GET-MYLIST",
                "나의 내역 조회", myList));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "VOTE_STATUS_FETCH_FAILED"})
    @Operation(summary = "투표 현황 조회(미완)")
    @GetMapping("/vote-status/{ledgerEntryId}")
    public ResponseEntity<?> getVoteStatus(@PathVariable("ledgerEntryId") String ledgerEntryId){
        VoteDto.setVodeDto voteStatus = ledgerService.getVoteStatus(ledgerEntryId);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "VOTE-STATUS",
                "투표 현황 조회", voteStatus));
    }
    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "ONE_THEME_BALANCE_FETCH_FAILED"})
    @Operation(summary = "하나의 테마 잔액 조회(미완)")
    @ApprovedOnly
    @GetMapping("/theme-balance/{theme}")
    public ResponseEntity<?> getThemeBalance(@PathVariable("theme") String theme){
        VoteDto.ThemeBalanceDto voteStatus = ledgerService.getThemeBalance(theme);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "GET-ONE-THEME-BALANCE",
                "하나의 테마 잔액 조회", voteStatus));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "TOTAL_BALANCE_FETCH_FAILED"})
    @Operation(summary = "전체 잔액 조회(미완)")
    @ApprovedOnly
    @GetMapping("/theme-balance/all")
    public ResponseEntity<?> themeAll(){
        List<VoteDto.ThemeBalanceDto> allThemeBalance = ledgerService.getAllThemeBalance();
        return ResponseEntity.ok(new BaseApiResponse<>(200, "GET-ALL-THEME-BALANCE",
                "전체 잔액 조회", allThemeBalance));
    }
}

