package Baeksa.money.domain.committee.service;

import Baeksa.money.domain.committee.event.CommitteeCountRequestEvent;
import Baeksa.money.domain.committee.event.PendingRequestEvent;
import Baeksa.money.domain.committee.event.StudentCountRequestEvent;
import Baeksa.money.domain.committee.event.UserStatusRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisEventListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @EventListener
    public void handleStudentCountRequestEvent(StudentCountRequestEvent event) {
        try {
            // Redis에 메시지 발행
            redisTemplate.convertAndSend(event.getChannel(), event.getMessage());
            log.debug("학생 수 요청 메시지 발행 완료: 채널={}, 메시지={}", event.getChannel(), event.getMessage());
        } catch (Exception e) {
            log.error("학생 수 요청 메시지 발행 실패", e);
        }
    }

    @EventListener
    public void handleCommitteeCountRequestEvent(CommitteeCountRequestEvent event) {
        try {
            // Redis에 메시지 발행
            redisTemplate.convertAndSend(event.getChannel(), event.getMessage());
            log.debug("학생회 수 요청 메시지 발행 완료: 채널={}, 메시지={}", event.getChannel(), event.getMessage());
        } catch (Exception e) {
            log.error("학생회 수 요청 메시지 발행 실패", e);
        }
    }

    @EventListener
    public void handlePendingRequestsEvent(PendingRequestEvent event) {
        try {
            // Redis에 메시지 발행
            redisTemplate.convertAndSend(event.getChannel(), event.getMessage());
            log.debug("대기 중인 가입 요청 목록 요청 메시지 발행 완료: 채널={}, 메시지={}", event.getChannel(), event.getMessage());
        } catch (Exception e) {
            log.error("대기 중인 가입 요청 목록 요청 메시지 발행 실패", e);
        }
    }

    @EventListener
    public void handleUserStatusRequestEvent(UserStatusRequestEvent event) {
        try {
            // 요청 객체 생성
            Map<String, String> requestData = new HashMap<>();
            requestData.put("requestId", event.getUserRequestId());

            // JSON 변환 및 Redis에 메시지 발행
            String message = objectMapper.writeValueAsString(requestData);
            redisTemplate.convertAndSend(event.getChannel(), message);

            log.debug("사용자 가입 상태 요청 메시지 발행 완료: 채널={}, 메시지={}", event.getChannel(), message);
        } catch (JsonProcessingException e) {
            log.error("사용자 가입 상태 요청 메시지 JSON 변환 실패", e);
        } catch (Exception e) {
            log.error("사용자 가입 상태 요청 메시지 발행 실패", e);
        }
    }
}