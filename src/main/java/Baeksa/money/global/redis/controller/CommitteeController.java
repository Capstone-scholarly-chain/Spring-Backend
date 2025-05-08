package Baeksa.money.global.redis.controller;

import Baeksa.money.domain.Dto.MemberDto;
import Baeksa.money.domain.Entity.MemberEntity;
import Baeksa.money.domain.Service.MemberService;
import Baeksa.money.domain.enums.Status;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pubsub/committee")
@Tag(name = "이벤트", description = "학생회 회원 등록/장부 등록 API")
@RequiredArgsConstructor
public class CommitteeController {

    private final RedisPublisher redisPublisher;
    private final MemberService memberService;
    private final RedisService redisService;
    private final FcmService fcmService;
    private final RedisTemplate redisTemplate;


    @Operation(description = "학생회 가입 신청 - 학생회 화면")
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



    @Operation(description = "학생회 가입 승인 - 학생회화면")
    @PatchMapping("/approve")
    public ResponseEntity<?> approve(@RequestBody RedisDto.pubDto redisDto,
                                     @RequestHeader("Authorization") String authToken,
                                     @AuthenticationPrincipal CustomUserDetails userDetails){

        memberService.ValidAccess(authToken);

        //학생회 정보 - userDetails
        //승인할 학생 정보 - redisDto
        Map map;
        MemberEntity byId = memberService.findById(redisDto.getApplicantId());
        MemberDto dto = MemberDto.builder()
                .id(byId.getId())
                .studentId(byId.getStudentId())
                .username(byId.getUsername())
                .role(byId.getRole())
                .status(byId.getStatus())
                .approvals(new ArrayList())
                .rejections(new ArrayList())
                .timestamp(byId.getTimestamp())
                .build();

        try {
            map = new HashMap<String, Object>();

            map.put("id", "REQ_STUDENT_" + dto.getTimestamp().toString() + "_" + dto.getStudentId());
            map.put("applicantId", dto.getStudentId());
            map.put("username", dto.getUsername());
            map.put("role", dto.getRole().toString());
            map.put("timestamp", LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            List<Long> rejections = dto.getRejections();
            List<Long> approvals = dto.getApprovals();

            //reject의 경우
            if(!redisDto.isActive()){
                if(dto.getStatus() == Status.PENDING){
                    memberService.reject(dto.getStudentId());
                    // 반대한 학번 리스트
                    rejections.add(userDetails.getStudentId());
                    dto.setRejections(rejections);
                    map.put("rejections", rejections);

                } else {
                    map.put("status", dto.getStatus().toString());
                    throw new CustomException(ErrorCode.ALREADY_STATUS);
                }

            } else {
                if(dto.getStatus() == Status.PENDING){
                    memberService.approve(dto.getStudentId());
                    // 찬성한 학번 리스트
                    approvals.add(userDetails.getStudentId());
                    dto.setApprovals(approvals);
                    map.put("approvals", approvals);

                } else {
                    map.put("status", dto.getStatus().toString());
                    throw new CustomException(ErrorCode.ALREADY_STATUS);
                }
            }
            redisPublisher.publish("spring:request:approve", map);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //메시지를 담고
        //access되어있는 사람들한테 반복문으로 fcm알림을 날린다던지 하면 됨

        return ResponseEntity.ok(new MemberDto.ApiResponse<>(201, "PUBSUB-APPROVE", "학생회 가입 승인", map));
    }



    //theme, amount, description, entryType
    @Operation(description = "학생회 출금 기입 신청 - 학생회 화면")
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestHeader("Authorization") String authToken,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestBody RedisDto.LedgerDto ledgerDto) {

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

        return ResponseEntity.ok(new MemberDto.ApiResponse<>(201, "PUBSUB-LEDGER", "학생회 출금 기입 요청", map));
    }

// spring:request:approve-deposit
//{
//    "approverId": "council456",
//        "ledgerEntryId": "LEDGER_1619716110_student123",
//        "timestamp": 1619716200
//}


    //학생이 입금 신청한 목록 > 하나 선택 > LEDGER_TIMESTAMP_학번 형식
    @Operation(description = "학생회 입금 승인 - 학생회 화면")
    @PostMapping("/approve-deposit")
        public ResponseEntity<?> deposit(@RequestHeader("Authorization") String authToken,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RedisDto dto){

        memberService.ValidAccess(authToken);

        Map map = new HashMap<String, Object>();

        Long studentId = dto.getStudentId();
        log.info("학생 ID: {}", studentId);
        String id = (String) redisTemplate.opsForValue().get("LEDGER_" + studentId);
//            Object redisValue = redisTemplate.opsForValue().get("LEDGER_" + studentId);
//            String id = redisService.unwrapRedisString(redisValue);
//            log.info("Redis raw value: {}", redisValue);
        log.info("Redis raw value: {}", id);

        if (id == null) {
            throw new CustomException(ErrorCode.NO_DEPOSIT);
        }

        if(dto.isAgree()){
            map.put("approverId", userDetails.getStudentId());   //학생회
            map.put("ledgerEntryId", id);   //입금 요청한 학생
            map.put("agree", dto.isAgree());

            String[] parts = redisService.getParts(id);
            map.put("timestamp", parts[2]);

            redisPublisher.publish("spring:request:ledger", map);

        } else {
            map.put("approverId", userDetails.getStudentId());   //학생회
            map.put("ledgerEntryId", id);   //입금 요청한 학생
            map.put("agree", dto.isAgree());

            String[] parts = redisService.getParts(id);
            map.put("timestamp", parts[2]);

            redisPublisher.publish("spring:request:ledger", map);
        }

        return ResponseEntity.ok(new MemberDto.ApiResponse<>(201, "PUBSUB-APPROVE-DEPOSIT", "학생회가 입금 내역 승인", map));
    }


}

