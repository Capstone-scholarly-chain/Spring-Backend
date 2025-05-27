package Baeksa.money.domain.student.service;

import Baeksa.money.domain.committee.service.RequestResponseTracker;
import Baeksa.money.domain.streams.service.RedisStreamProducer;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RequestResponseTracker requestTracker;
    private final RedisStreamProducer redisStreamProducer;

    // 채널 및 키 상수
    private static final String STUDENT_COUNT_KEY = "membership:student-count";
    private static final String STUDENT_COUNT_CHANNEL = "spring:request:student-count";

    private static final int REQUEST_TIMEOUT_SECONDS = 3;



    // 학생 수 조회
    public String findStudentCount() {
        try {
            String count = redisTemplate.opsForValue().get(STUDENT_COUNT_KEY);

            if (count == null) {
                count = requestStudentCount();
            }
            return count;
        } catch (Exception e) {
            log.error("학생 수 조회 실패", e);
            throw new CustomException(ErrorCode.GET_COUNTS_FAILED);
        }
    }
    /// 조회할 데이터가 없을 때
    private String requestStudentCount() throws InterruptedException {
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
            String count = redisTemplate.opsForValue().get(STUDENT_COUNT_KEY);
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
//    /// 조회할 데이터가 없을 때
//    private Integer requestStudentCount() throws InterruptedException {
//        // 요청 식별자 생성
//        String requestId = UUID.randomUUID().toString();
//
//        // 요청 등록
//        CountDownLatch latch = requestTracker.registerRequest(requestId);
//
//        try {
//            // Redis에 직접 메시지 발행
//            String message = "학생 수 요청:" + requestId;
//            redisTemplate.convertAndSend(STUDENT_COUNT_CHANNEL, message);
//            log.debug("학생 수 요청 발행: {}", requestId);
//
//            // 응답 대기
//            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
//
//            // 응답 확인
//            if (!receivedInTime || !requestTracker.isRequestSuccessful(requestId)) {
//                log.warn("학생 수 응답 타임아웃: {}", requestId);
//                throw new CustomException(ErrorCode.STUDENT_COUNT_TIMEOUT);
//            }
//
//            // 응답을 받았으면 Redis에서 다시 조회
//            Integer count = redisTemplateInteger.opsForValue().get(STUDENT_COUNT_KEY);
//            if (count == null) {
//                log.warn("학생 수 응답 후에도 값이 없음: {}", requestId);
//                throw new CustomException(ErrorCode.STUDENT_COUNT_NOT_AVAILABLE);
//            }
//
//            log.debug("학생 수 조회 성공: {}", count);
//            return count;
//        } finally {
//            // 완료된 요청 정리
//            requestTracker.cleanupRequest(requestId);
//        }
//    }

