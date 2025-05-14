package Baeksa.money.domain.committee.service;

import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommitteeService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Integer> redisTemplateInteger;
    private final ObjectMapper objectMapper;
    private final RequestResponseTracker requestTracker;


    private static final String COMMITTEE_COUNT_KEY = "membership:council-count";
    private static final String COMMITTEE_COUNT_CHANNEL = "spring:request:council-count";

    private static final int REQUEST_TIMEOUT_SECONDS = 3;
    // ===== 비즈니스 로직 메서드들 ====

    // 학생회 조직원 수 조회
    public Integer findCommitteeCount() {
        try {
            String countStr = redisTemplate.opsForValue().get(COMMITTEE_COUNT_KEY);
            Integer count = (countStr != null) ? Integer.parseInt(countStr) : null;
//            Integer count = redisTemplateInteger.opsForValue().get(COMMITTEE_COUNT_KEY);

            if (count == null) {
                count = requestCommitteeCount();
            }
            log.info("캐시된 값: {}", count);
            return count;
        } catch (Exception e) {
            log.error("학생회 조직원 수 조회 실패", e);
            throw new CustomException(ErrorCode.GET_STUDENTS_FAILED);
        }
    }

    private Integer requestCommitteeCount() throws InterruptedException {
        // 요청 식별자 생성
        String requestId = UUID.randomUUID().toString();

        // 요청 등록
        CountDownLatch latch = requestTracker.registerRequest(requestId);

        try {
            // Redis에 직접 메시지 발행
//            String message = requestId;
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("requestId", requestId);

            String message = null;
            try {
                message = objectMapper.writeValueAsString(messageMap);
                log.info("message: {}", message);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            redisTemplate.convertAndSend(COMMITTEE_COUNT_CHANNEL, message);
            log.info("학생회 조직원 수 요청 발행: {}", requestId);

            // 응답 대기
            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 응답 확인
            if (!receivedInTime) {
                log.warn("time: {}", receivedInTime);
                if(!requestTracker.isRequestSuccessful(requestId))
                    log.warn("대기 중인 가입 요청 목록 응답 타임아웃: {}", requestId);
                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            // 응답을 받았으면 Redis에서 다시 조회
            String countStr = redisTemplate.opsForValue().get(COMMITTEE_COUNT_KEY);
            Integer count = (countStr != null) ? Integer.parseInt(countStr) : null;
            if (count == null) {
                log.warn("학생회 조직원 수 응답 후에도 값이 없음: {}", requestId);
                throw new CustomException(ErrorCode.STUDENT_COUNT_NOT_AVAILABLE);
            }

            log.debug("학생회 조직원 수 조회 성공: {}", count);
            return count;
        } finally {
            // 완료된 요청 정리
            requestTracker.cleanupRequest(requestId);
        }
    }
}
