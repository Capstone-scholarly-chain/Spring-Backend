package Baeksa.money.domain.student.service;

import Baeksa.money.domain.auth.Dto.MemberDto;
import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.Service.MemberService;
import Baeksa.money.domain.auth.enums.Status;
import Baeksa.money.domain.student.event.LedgerRequestEvent;
import Baeksa.money.domain.student.event.MembershipApplyEvent;
import Baeksa.money.domain.student.event.WithdrawApproveEvent;
import Baeksa.money.domain.student.event.WithdrawRejectEvent;
import Baeksa.money.domain.student.dto.StudentDto;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberService memberService;
    private final ApplicationEventPublisher eventPublisher;


    public void publish(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }

    public Map<String, Object> apply(String studentId) {
        //로그인한 학생이 회원가입 후 신청
        Map<String, Object> map = new HashMap<>();
        try {
            MemberEntity byId = memberService.findById(studentId); //로그인한 학생 정보 꺼내기
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

            publish("spring:request:membership", map);

            return map;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.STUDENT_APPLY);
        }
    }

    public Map<String, Object> applyLedger(String councilId, StudentDto.LedgerDto ledgerDto) {

        MemberEntity member = memberService.findById(councilId);
        ValidStatus(member, member.getStatus());

        Map<String, Object> map = new HashMap<>();
        try {
            map.put("userId", councilId);
            map.put("theme", ledgerDto.getTheme());
            map.put("amount", ledgerDto.getAmount());
            map.put("description", ledgerDto.getDescription());
            map.put("documentURL", ledgerDto.getDocumentURL());

            // nestjs 한테 Pub
//            publish("spring:request:ledger", map);

            return map;

        } catch (Exception e) {
            throw new CustomException(ErrorCode.STUDENT_APPLY_LEDGER);
        }
    }

    public Map<String, Object> voteWithdraw(String voterId, StudentDto.VoteDto dto) {

        Map<String, Object> map = new HashMap<>();
        try {
            map.put("voterId", voterId);   //투표한 학생
            if (dto.isVote()) {
                map.put("vote", "approve");
            } else {
                map.put("vote", "reject");
            }
            map.put("ledgerEntryId", dto.getLedgerEntryId());   //출금 기입한 학생회

            publish("spring:request:vote-withdraw", map);

            return map;

        } catch (Exception e) {
            throw new CustomException(ErrorCode.STUDENT_WITHDRAW);
        }
    }

    public void ValidStatus(MemberEntity entity, Status status){
        if(entity.getStatus() != Status.APPROVE){
            throw new CustomException(ErrorCode.NOTSET_STATUS);
        }
    }
}

