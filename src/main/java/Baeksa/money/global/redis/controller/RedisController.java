package Baeksa.money.global.redis.controller;

import Baeksa.money.domain.Dto.MemberDto;
import Baeksa.money.domain.Entity.MemberEntity;
import Baeksa.money.domain.Service.MemberService;
import Baeksa.money.global.fcm.FcmService;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.ErrorCode;
import Baeksa.money.global.jwt.CustomUserDetails;
import Baeksa.money.global.redis.RedisDto;
import Baeksa.money.global.redis.service.RedisPublisher;
import Baeksa.money.global.redis.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pubsub/student")
@Tag(name = "이벤트", description = "학생 회원 등록/장부 등록 API")
@RequiredArgsConstructor
public class RedisController {

    private final RedisPublisher redisPublisher;
    private final MemberService memberService;
    private final RedisService redisService;
    private final FcmService fcmService;
    private final RedisTemplate redisTemplate;


    @Operation(description = "학생 가입 신청 - 학생 화면")
    @PostMapping("/membership")
    public ResponseEntity<?> membership(@RequestBody RedisDto redisDto,
                           @RequestHeader("Authorization") String authToken,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {

        memberService.ValidAccess(authToken);

        Map map = new HashMap<String, Object>();
        if(redisDto.isAgree()){
            MemberEntity byId = memberService.findById(userDetails.getStudentId());
            MemberDto dto = MemberDto.builder()
                    .studentId(byId.getStudentId())
                    .username(byId.getUsername())
                    .role(byId.getRole())
                    .timestamp(byId.getTimestamp())
                    .build();

            map.put("studentId", dto.getStudentId());
            map.put("applicantId", dto.getStudentId());
            map.put("username", dto.getUsername());
            map.put("role", dto.getRole().toString());
            map.put("timestamp", dto.getTimestamp().toString());

            redisPublisher.publish("spring:request:membership", map);

//                fcmService.sendMessage(fcmToken, "학생 가입 신청", dto.getUsername() + "이 가입 신청했습니다.");
        }
        return ResponseEntity.ok(new MemberDto.ApiResponse<>(200, "PUBSUB-MEMBERSHIP", "학생 가입 신청", map));
    }

    //프론트는 theme, amount, description, entryType만 넘겨주면 됨
    @Operation(description = "학생이 입금 기입 요청을 보냄 - 학생 화면")
    @PostMapping("/ledger")
    public ResponseEntity<?> ledger(@RequestHeader("Authorization") String authToken,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestBody RedisDto.LedgerDto ledgerDto){

        memberService.ValidAccess(authToken);

        MemberEntity member = memberService.findById(userDetails.getStudentId());
        redisService.ValidStatus(member, member.getStatus());

        Map map = new HashMap<String, Object>();
        map.put("studentId", userDetails.getStudentId());
        map.put("theme", ledgerDto.getTheme());
        map.put("amount", ledgerDto.getAmount());
        map.put("description", ledgerDto.getDescription());
        map.put("entryType", ledgerDto.getEntryType());
        map.put("timestamp", LocalDateTime.now(ZoneId.of("Asia/Seoul")));

        redisPublisher.publish("spring:request:ledger", map);

        return ResponseEntity.ok(new MemberDto.ApiResponse<>(201, "PUBSUB-LEDGER", "입금 기입 요청", map));
    }

//    {
//        "approverId": "student123", "ledgerEntryId": "LEDGER_1619716310_council456",
//            "timestamp": 1619716400
//    }
    //학생이 찬반하는 투표 형식으로 추후에 바뀔 수 있음
    @Operation(description = "학생 출금 승인 - 학생 화면")
    @PostMapping("approve-withdraw")
    public ResponseEntity<?> withdraw(@RequestHeader("Authorization") String authToken,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody RedisDto dto){

        memberService.ValidAccess(authToken);

        Map map = new HashMap<String, Object>();

        Long studentId = dto.getStudentId();
        log.info("신청한 학생회 ID: {}", studentId);
        String id = (String) redisTemplate.opsForValue().get("LEDGER_" + studentId);
        log.info("Redis raw value: {}", id);

        if (id == null) {
            throw new CustomException(ErrorCode.NO_WITHDRAW);
        }

        if(dto.isAgree()){
            map.put("approverId", userDetails.getStudentId());   //승인한 학생
            map.put("ledgerEntryId", id);   //출금 기입한 학생회
            map.put("agree", dto.isAgree());

            String[] parts = redisService.getParts(id);
            map.put("timestamp", parts[1]);

            redisPublisher.publish("spring:request:approve-withdraw", map);
        }
//        else {
//            map.put("approverId", userDetails.getStudentId());   //승인한 학생
//            map.put("ledgerEntryId", id);   //출금 기입한 학생회
//            map.put("agree", dto.isAgree());
//
//            String[] parts = redisService.getParts(id);
//            map.put("timestamp", parts[2]);
//
//            redisPublisher.publish("spring:request:approve-withdraw", map);
//        }
        return ResponseEntity.ok(new MemberDto.ApiResponse<>(201, "PUBSUB-APPROVE-WITHDRAW", "출금 기입 승인", map));
    }

    //학생이 찬반하는 투표 형식으로 추후에 바뀔 수 있음
    @Operation(description = "학생 출금 거부 - 학생 화면")
    @PostMapping("reject-withdraw")
    public ResponseEntity<?> withdraw2(@RequestHeader("Authorization") String authToken,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody RedisDto dto){

        memberService.ValidAccess(authToken);

        Map map = new HashMap<String, Object>();

        Long studentId = dto.getStudentId();
        log.info("신청한 학생회 ID: {}", studentId);
        String id = (String) redisTemplate.opsForValue().get("LEDGER_" + studentId);
        log.info("Redis raw value: {}", id);

        if (id == null) {
            throw new CustomException(ErrorCode.NO_WITHDRAW);
        }

        if(!dto.isAgree()){
            map.put("approverId", userDetails.getStudentId());   //승인한 학생
            map.put("ledgerEntryId", id);   //출금 기입한 학생회
            map.put("agree", dto.isAgree());    //거절
            map.put("reason", dto.getReason()); //이유: 예산 초과

            String[] parts = redisService.getParts(id);
            map.put("timestamp", parts[1]);

            redisPublisher.publish("spring:request:reject-withdraw", map);
        }
        return ResponseEntity.ok(new MemberDto.ApiResponse<>(201, "PUBSUB-APPROVE-WITHDRAW", "출금 기입 거절", map));
    }

}
