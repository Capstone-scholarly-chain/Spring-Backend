package Baeksa.money.domain.committee.service;

import Baeksa.money.domain.streams.service.RedisStreamProducer;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.service.RequestResponseTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommitteeService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RequestResponseTracker requestTracker;
    private final RedisStreamProducer redisStreamProducer;

    private static final String COMMITTEE_COUNT_KEY = "membership:council-count";
    private static final int REQUEST_TIMEOUT_SECONDS = 3;
    // ===== 비즈니스 로직 메서드들 ====

    // 학생회 조직원 수 조회
    public String findCommitteeCount() {
        try {
            String count = redisTemplate.opsForValue().get(COMMITTEE_COUNT_KEY);

            if (count == null) {
                count = requestCommitteeCount();
            }
            log.info("캐시된 값: {}", count);
            return count;
        } catch (Exception e) {
            log.error("학생회 조직원 수 조회 실패", e);
            throw new CustomException(ErrorCode.GET_COUNTS_FAILED);
        }
    }

    private String requestCommitteeCount() throws InterruptedException {
        // Redis Stream으로 메시지 발송 (recordId 반환받음)
        String recordId = redisStreamProducer.sendMessage("학생 수 요청", "GET_STUDENT_COUNT").toString();
        log.info("학생 수 요청 발송: recordId={}", recordId);

        // recordId로 요청 등록 (CountDownLatch 생성)
        CountDownLatch latch = requestTracker.registerRequest(recordId);

        try {
            // 응답 대기
            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!receivedInTime) {
                log.warn("학생 수 응답 타임아웃: recordId={}", recordId);
                throw new CustomException(ErrorCode.COUNT_TIMEOUT);
            }

            // 요청 성공 여부 확인
            if (!requestTracker.isRequestSuccessful(recordId)) {
                log.warn("학생 수 요청 실패: recordId={}", recordId);
                throw new CustomException(ErrorCode.GET_COUNTS_FAILED);
            }

            // 응답을 받았으면 Redis에서 다시 조회
            String count = redisTemplate.opsForValue().get(COMMITTEE_COUNT_KEY);
            if (count == null) {
                log.warn("학생 수 응답 후에도 캐시에 값이 없음: recordId={}", recordId);
                throw new CustomException(ErrorCode.COUNT_NOT_AVAILABLE);
            }

            log.info("학생 수 조회 성공: count={}, recordId={}", count, recordId);
            return count;

        } finally {
            // 완료된 요청 정리
            requestTracker.cleanupRequest(recordId);
        }
    }
}
