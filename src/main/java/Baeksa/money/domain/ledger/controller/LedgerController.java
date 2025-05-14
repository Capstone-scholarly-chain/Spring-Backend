package Baeksa.money.domain.ledger.controller;

import Baeksa.money.domain.ledger.LedgerPubService;
import Baeksa.money.domain.ledger.LedgerService;
import Baeksa.money.domain.ledger.dto.PendingDepositDto;
import Baeksa.money.domain.ledger.dto.VoteDto;
import Baeksa.money.global.config.swagger.ApiErrorCodeExample;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import Baeksa.money.global.excepction.code.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/pubsub/ledger")
@RestController
@RequiredArgsConstructor
@Tag(name = "Ledger관련 api")
public class LedgerController {

    private final LedgerService ledgerService;
    private final LedgerPubService ledgerPubService;

    @Operation(summary = "pubsub test")
    @GetMapping("/test")
    public ResponseEntity<?> test(){
        ledgerPubService.getTest();
        return ResponseEntity.ok(new BaseApiResponse<>(200, "PUBSUB-REJECT", "학생회 가입 거절", null));
    }

    //내가 키 쪼회하고 3초 기다리는
//    @Operation(summary = "조회 test")
//    @GetMapping("/test2")
//    public ResponseEntity<?> test2(){
//        ledgerService.
//        return ResponseEntity.ok(new BaseApiResponse<>(200, "PUBSUB-REJECT", "학생회 가입 거절", null));
//    }


//    VOTE_REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_007", "투표 등록 실패"),
//    ONE_THEME_BALANCE_REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_008", "하나의 테마 잔액 등록 실패"),
//    ALL_THEME_BALANCE_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_009", "테마 잔액 등록 실패"),
//
//    DEPOSIT_CACHE_COMPLETE(HttpStatus.OK, "SUB_010", "입금 항목 캐싱 완료"),
//    WITHDRAW_CACHE_COMPLETE(HttpStatus.OK, "SUB_011", "출금 항목 캐싱 완료"),
//
//    VOTE_STATUS_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_012", "투표 현황 조회 실패"),
//    ONE_THEME_BALANCE_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_013", "하나의 테마 잔액 조회 실패"),
//    TOTAL_BALANCE_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_014", "전체 잔액 조회 실패"),
    @ApiErrorCodeExample(value = ErrorCode.class, include = {"PENDING_DEPOSIT_FETCH_FAILED"})
    @Operation(summary = "대기중인 입금 항목 조회")
    @GetMapping("/deposits")
    public ResponseEntity<?> deposits(){
        List<String> pendingDeposits = ledgerService.getPendingDeposits();
        return ResponseEntity.ok(new BaseApiResponse<>(200, "GET-DEPOSITS",
                "대기중인 입금 항목 조회", pendingDeposits));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"PENDING_WITHDRAW_FETCH_FAILED"})
    @Operation(summary = "대기중인 출금 항목 조회")
    @GetMapping("/withdrawals")
    public ResponseEntity<?> withdrawals(){
        List<String> pendingWithdrawals = ledgerService.getPendingWithdrawals();
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
    @Operation(summary = "나의 내역 조회")
    @GetMapping("/{studentId}")
    public ResponseEntity<?> getMyList2(@PathVariable("studentId") String studentId){
        List<Map<String, Object>> myList = ledgerService.getMyList2(studentId);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "GET-MYLIST",
                "나의 내역 조회", myList));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "DEPOSIT_CACHE_FAILED"})
    @Operation(summary = "입금 항목 캐싱")
    @PostMapping("/pending-deposits")
    public ResponseEntity<?> cachePendingDeposits(@RequestBody List<PendingDepositDto> deposits) {
        ledgerService.cachePendingDeposits(deposits);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "CACHE-PENDING-DEPOSITS", "입금 항목 캐싱 완료", null));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "WITHDRAW_CACHE_FAILED"})
    @Operation(summary = "출금 항목 캐싱")
    @PostMapping("/pending-withdraws")
    public ResponseEntity<?> cachePendingWithdraws(@RequestBody List<PendingDepositDto> deposits) {
        ledgerService.cachePendingWithdraws(deposits);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "CACHE-PENDING-WITHDRAW", "출금 항목 캐싱 완료", null));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "VOTE_REGISTER_FAILED"})
    @Operation(summary = "투표 등록")
    @PostMapping("/vote-status")
    public ResponseEntity<?> setVoteStatus(@RequestBody VoteDto.setVodeDto votes){
        VoteDto.setVodeDto result = ledgerService.setVoteStatus(votes, votes.getEntryId());
        return ResponseEntity.ok(new BaseApiResponse<>(200, "VOTE-STATUS",
                "투표 등록", result));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "ONE_THEME_BALANCE_REGISTER_FAILED"})
    @Operation(summary = "하나의 테마 잔액 등록")
    @PostMapping("/theme-balance")
    public ResponseEntity<?> setThemeBalance(@RequestBody VoteDto.ThemeBalanceDto themeBalanceDto) {
        VoteDto.ThemeBalanceDto result = ledgerService.setThemeBalance(themeBalanceDto, themeBalanceDto.getTheme());
        return ResponseEntity.ok(new BaseApiResponse<>(200, "POST-ONE-THEME-BALLANCE",
                "하나의 테마 잔액 등록", result));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "ALL_THEME_BALANCE_FETCH_FAILED"})
    @Operation(summary = "모든 테마 잔액 등록")
    @PostMapping("/theme/all")
    public ResponseEntity<?> setThemeAll(@RequestBody List<VoteDto.ThemeBalanceDto> balances) {
        List<VoteDto.ThemeBalanceDto> themeBalanceDtos = ledgerService.setAllThemeBalance(balances);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "SET-ALL-BALANCES", "모든 테마 잔액 조회", themeBalanceDtos));
    }


    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "DEPOSIT_CACHE_COMPLETE"})
    @Operation(summary = "입금 항목 캐싱 완료")
    @GetMapping("/pending-deposits")
    public ResponseEntity<?> getPendingDeposits() {
        List<PendingDepositDto> result = ledgerService.getPendingDummyDeposits();

        return ResponseEntity.ok(new BaseApiResponse<>(200, "CACHE-PENDING-DEPOSITS", "입금 항목 캐싱 완료", result));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "WITHDRAW_CACHE_COMPLETE"})
    @Operation(summary = "출금 항목 캐싱 완료")
    @GetMapping("/pending-withdraws")
    public ResponseEntity<?> getPendingWithdraws() {
        List<PendingDepositDto> result = ledgerService.getPendingDummyWithdraws();

        return ResponseEntity.ok(new BaseApiResponse<>(200, "CACHE-PENDING-WITHDRAWS", "출금 항목 캐싱 완료", result));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "VOTE_STATUS_FETCH_FAILED"})
    @Operation(summary = "투표 현황 조회")
    @GetMapping("/vote-status/{key}")
    public ResponseEntity<?> getVoteStatus(@PathVariable("key") String key){
        VoteDto.setVodeDto voteStatus = ledgerService.getVoteStatus(key);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "VOTE-STATUS",
                "투표 현황 조회", voteStatus));
    }
    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "ONE_THEME_BALANCE_FETCH_FAILED"})
    @Operation(summary = "하나의 테마 잔액 조회")
    @GetMapping("/theme-balance/{theme}")
    public ResponseEntity<?> getThemeBalance(@PathVariable("theme") String theme){
        VoteDto.ThemeBalanceDto voteStatus = ledgerService.getThemeBalance(theme);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "GET-ONE-THEME-BALANCE",
                "하나의 테마 잔액 조회", voteStatus));
    }

    @ApiErrorCodeExample(value = ErrorCode.class, include = {"JSON_FAILED", "TOTAL_BALANCE_FETCH_FAILED"})
    @Operation(summary = "전체 잔액 조회")
    @GetMapping("/theme/all")
    public ResponseEntity<?> themeAll(){
        List<VoteDto.ThemeBalanceDto> allThemeBalance = ledgerService.getAllThemeBalance();
        return ResponseEntity.ok(new BaseApiResponse<>(200, "GET-ALL-THEME-BALANCE",
                "전체 잔액 조회", allThemeBalance));
    }
}

