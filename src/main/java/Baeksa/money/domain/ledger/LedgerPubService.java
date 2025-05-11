package Baeksa.money.domain.ledger;

import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.Service.MemberService;
import Baeksa.money.domain.auth.enums.Status;
import Baeksa.money.domain.committee.dto.CommitteeDto;
import Baeksa.money.domain.ledger.dto.TestDto;
import Baeksa.money.domain.student.dto.StudentDto;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerPubService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }

    public void getTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", "22");
        map.put("requestId", "22");
        map.put("theme", "22");
        map.put("amount", "22");
        map.put("description", "22");
        map.put("documentURL", "22");
        publish("spring:request:test", "test돼라 제발요");
    }

//    public void getTest2(TestDto dto) {
//        try {
//            String json = objectMapper.writeValueAsString(dto);
//            publish("nestjs:request:test", json); // 원하는 채널 이름
//            log.info("✅ Redis에 메시지 발행 완료: {}", json);
//        } catch (JsonProcessingException e) {
//            throw new CustomException(ErrorCode.JSON_FAILED);
//        }
//    }

///아래를 보내고 메세지를 받아서 나는 키를 조회할 거임
/// 아래는 끝
    public void getPendingDeposits() {
        publish("spring:request:pending-deposits", "대기중인 입금 항목 조회");
    }

    public void getPendingWithdrawals() {
        publish("spring:request:pending-withdraws", "대기중인 출금 항목 조회");
    }

    public void getVoteStatus(StudentDto.VoteDto voteDto) {
        String ledgerEntryId = voteDto.getLedgerEntryId();
        publish("spring:request:vote-status", "ledgerEntryId");
    }

    public void getThemeBalance() {
        publish("spring:request:theme-balance", "테마 잔액 조회");
    }

    public void getAllThemeBalance() {
        publish("spring:request:alltheme-balance", "모든 테마 잔액 조회");
    }


}
