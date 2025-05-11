package Baeksa.money.domain.committee.service;


import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.Service.MemberService;
import Baeksa.money.domain.auth.enums.Status;
import Baeksa.money.domain.committee.dto.CommitteeDto;
import Baeksa.money.domain.committee.event.*;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommitteePublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberService memberService;
    private final RedisService redisService;
    private final ApplicationEventPublisher eventPublisher;

    public void publish(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }


    //프론트한테는 신청한 학생 학번만 받기
    public Map<String, Object> approveStudent(String councilId, CommitteeDto.approveDto approveDto) {
        //학생회 정보 - userDetails, 승인할 학생회 정보: approveDto에서 requestId 꺼내기

        MemberEntity requestStudent = memberService.findById(approveDto.getRequestId());

        if (requestStudent.getStatus() == Status.PENDING) {
            Map<String, Object> map = new HashMap<>();
            try {
                //nest에서 타임스탬프 찍어서 id 만들거임
                //난 id로 안 보내도 되지 -> 근데 이걸 이제 클라가 해줌!
//                committeePubSubService.getRequestId(approveDto.getRequestId());
                map.put("requestId", approveDto.getRequestId());
                map.put("applicantId", councilId);

//                publish("spring:request:approve", map);
                eventPublisher.publishEvent(new ApproveStudentEvent("spring:request:approve", map));
                return map;
            } catch (Exception e) {
                throw new CustomException(ErrorCode.COMMITTEE_APPROVE);
            }
        } else {
            throw new CustomException(ErrorCode.INVALID_APPROVAL);
        }
    }

    public Map<String, Object> rejectStudent(String councilId, CommitteeDto.rejectDto rejectDto) {

        MemberEntity requestStudent = memberService.findById(rejectDto.getRequestId());

        if (requestStudent.getStatus() == Status.PENDING) {
            Map<String, Object> map = new HashMap<>();

            try {
                map.put("requestId", rejectDto.getRequestId());
                map.put("applicantId", councilId);

//                publish("spring:request:reject", map);
                eventPublisher.publishEvent(new RejectStudentEvent("spring:request:reject", map));
                return map;
            } catch (Exception e) {
                throw new CustomException(ErrorCode.COMMITTEE_REJECT);
            }
        } else {
            throw new CustomException(ErrorCode.INVALID_APPROVAL);
        }
    }

    public Map<String, Object> applyWithdraw(String councilId, CommitteeDto.LedgerDto ledgerDto) {

        MemberEntity member = memberService.findById(councilId);
        redisService.ValidStatus(member, member.getStatus());

        Map<String, Object> map = new HashMap<>();
        try {
            map.put("userId", councilId);
            map.put("theme", ledgerDto.getTheme());
            map.put("amount", ledgerDto.getAmount());
            map.put("description", ledgerDto.getDescription());
            map.put("documentURL", ledgerDto.getDescription());

//            publish("spring:request:ledger-withdraw", map);
            eventPublisher.publishEvent(new WithdrawRequestEvent("spring:request:ledger-withdraw", map));

            return map;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.COMMITTEE_WITHDRAW);
        }
    }

    public Map<String, Object> approveDeposit(String councilId, String ledgerEntryId) {
        Map map = new HashMap<String, Object>();
        if (ledgerEntryId == null) {
            throw new CustomException(ErrorCode.NO_DEPOSIT);
        }
        try {
            map.put("approverId", councilId);   //학생회
            map.put("ledgerEntryId", ledgerEntryId);   //입금 요청한 학생

//            publish("spring:request:approve-deposit", map);
            eventPublisher.publishEvent(new ApproveDepositEvent("spring:request:approve-deposit", map));
            return map;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.COMMITTEE_APPROVE_DEPOSIT);
        }
    }

    public Map<String, Object> rejectDeposit(String councilId, String ledgerEntryId) {

        Map map = new HashMap<String, Object>();

        if (ledgerEntryId == null) {
            throw new CustomException(ErrorCode.NO_DEPOSIT);
        }
        try {
            map.put("rejectorId", councilId);   //학생회
            map.put("ledgerEntryId", ledgerEntryId);   //입금 요청한 학생

//            publish("spring:request:reject-deposit", map);
            eventPublisher.publishEvent(new RejectDepositEvent("spring:request:reject-deposit", map));
            return map;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.COMMITTEE_REJECT_DEPOSIT);
        }
    }

}
