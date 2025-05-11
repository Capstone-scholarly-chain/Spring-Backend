package Baeksa.money.domain.committee.service;

import Baeksa.money.domain.committee.event.*;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommitteePubSubService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Integer> redisTemplateInteger;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestResponseTracker requestTracker;

//    // 응답 채널 정의
//    private static final class ResponseChannels {
//        // 멤버십 관련
//        public static final String REGISTER_USER = "nestjs:response:register-user";
//        public static final String MEMBERSHIP_UPDATED = "nestjs:response:membership:updated";
//        public static final String MEMBERSHIP_APPROVE = "nestjs:response:membership:approve";
//        public static final String MEMBERSHIP_REJECTED = "nestjs:response:membership:rejected";
//    }

    private static final String STUDENT_COUNT_KEY = "membership:student-count";
    private static final String STUDENT_COUNT_CHANNEL = "spring:request:student-count";
    private static final String COMMITTEE_COUNT_KEY = "membership:council-count";
    private static final String COMMITTEE_COUNT_CHANNEL = "spring:request:council-count";
    private static final String PENDING_REGISTER_KEY = "membership:pending-requests";
    private static final String PENDING_REGISTER_CHANNEL = "spring:request:pending-register";
    private static final String REGISTER_STATUS_KEY_PREFIX = "membership:request:";
    private static final String REGISTER_STATUS_CHANNEL = "spring:request:register-user-status";

    private static final int REQUEST_TIMEOUT_SECONDS = 3;

//    public String getVoteResult(String ledgerEntryId) {
//        String value = redisTemplate.opsForValue().get(ledgerEntryId);
//        return value;
//    }
//
//    public List<String> getStudents() {
//        Set<String> keys = redisTemplate.keys("REQ_STUDENT_*");
//        List<String> result = new ArrayList<>();
//        if (keys != null) {
//            for (String key : keys) {
//                String value = redisTemplate.opsForValue().get(key);
//                System.out.println("Key: " + key + ", Value: " + value);
//                result.add(value);
//            }
//        }
//        return result;
//    }

    @EventListener
    public void handleRedisResponseEvent(RedisResponseEvent event) {
        log.info("Redis 응답 이벤트 수신: 채널={}, 요청ID={}", event.getChannel(), event.getRequestId());

        // 요청 ID에 해당하는 요청을 완료 처리
        requestTracker.markRequestCompleted(event.getRequestId());
    }

    // ===== 멤버십 관련 =====

    public void processRegisterUser(Map<String, Object> data) {
        log.info("학생 가입 요청 응답 처리: {}", data);
        // 여기에 필요한 비즈니스 로직 구현
    }

    public void processMembershipUpdate(Map<String, Object> data) {
        log.info("학생 요청 상태 업데이트 응답 처리: {}", data);
        // 여기에 필요한 비즈니스 로직 구현
    }

    public void processMembershipApproval(Map<String, Object> data) {
        log.info("학생 요청 승인 응답 처리: {}", data);
        // 여기에 필요한 비즈니스 로직 구현
    }

    public void processMembershipRejection(Map<String, Object> data) {
        log.info("학생 요청 거절 응답 처리: {}", data);
        // 여기에 필요한 비즈니스 로직 구현
    }


    // ===== 조회 관련 =====
//    public Map<String, String> getRequestId(String requestId) {
//        String value;
//        try {
//            value = redisTemplate.opsForValue().get("*" + requestId + "*");
//        } catch (Exception e) {
//            throw new CustomException(ErrorCode.MY_HISTORY_FETCH_FAILED);
//        }
//
//        try {
//            return objectMapper.readValue(value, new TypeReference<Map<String, String>>() {});
//        } catch (JsonProcessingException e) {
//            log.warn("JSON 파싱 실패. key: {}, value: {}", requestId, value);
//            throw new CustomException(ErrorCode.JSON_FAILED);
//        }
//    }


    /// /////5/11오늘 만든 수 조회
    public int findStudentCount() {
        try {
            Integer count = redisTemplateInteger.opsForValue().get(STUDENT_COUNT_KEY);

            if (count == null) {
                count = requestStudentCount();//아래 함수 호출
            }
            return count;
        } catch (Exception e) {
            log.error("학생 조직원 수 조회 실패", e);
            throw new CustomException(ErrorCode.GET_STUDENTS_FAILED);
        }
    }

    private Integer requestStudentCount() throws InterruptedException {
        // 요청 식별자 생성
        String requestId = UUID.randomUUID().toString();

        // 요청 등록
        CountDownLatch latch = requestTracker.registerRequest(requestId);

        try {
            // 요청 메시지 발행
            log.debug("학생 조직원 수 요청 발행: {}", requestId);
            eventPublisher.publishEvent(new StudentCountRequestEvent(STUDENT_COUNT_CHANNEL, requestId));

            // 응답 대기
            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 응답 확인
            if (!receivedInTime || !requestTracker.isRequestSuccessful(requestId)) {
                log.warn("학생 조직원 수 응답 타임아웃: {}", requestId);
                throw new CustomException(ErrorCode.STUDENT_COUNT_TIMEOUT);
            }

            // 응답을 받았으면 Redis에서 다시 조회
            Integer count = redisTemplateInteger.opsForValue().get(STUDENT_COUNT_KEY);
            if (count == null) {
                log.warn("학생 조직원 수 응답 후에도 값이 없음: {}", requestId);
                throw new CustomException(ErrorCode.STUDENT_COUNT_NOT_AVAILABLE);
            }

            log.debug("학생 조직원 수 조회 성공: {}", count);
            return count;
        } finally {
            // 완료된 요청 정리
            requestTracker.cleanupRequest(requestId);
        }
    }
    /// //////////////////에러 처리 집가서
    /// //////////////////////////////////////
    public int findCommitteeCount() {
        try {
            Integer count = redisTemplateInteger.opsForValue().get(COMMITTEE_COUNT_KEY);

            if (count == null) {
                count = requestCommitteeCount();//아래 함수 호출
            }
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
            // 요청 메시지 발행
            log.debug("학생 조직원 수 요청 발행: {}", requestId);
            eventPublisher.publishEvent(new CommitteeCountRequestEvent(COMMITTEE_COUNT_CHANNEL, "학생회 조직원 수 요청"));
            eventPublisher.publishEvent(new CommitteeCountRequestEvent(COMMITTEE_COUNT_CHANNEL, requestId));
            // 응답 대기
            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 응답 확인
            if (!receivedInTime || !requestTracker.isRequestSuccessful(requestId)) {
                log.warn("학생회 조직원 수 응답 타임아웃: {}", requestId);
                throw new CustomException(ErrorCode.STUDENT_COUNT_TIMEOUT);
            }

            // 응답을 받았으면 Redis에서 다시 조회
            Integer count = redisTemplateInteger.opsForValue().get(COMMITTEE_COUNT_KEY);
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

    // 대기 중인 가입 요청 조회
    public List<Map<String, Object>> findPendingRequests() {
        try {
            String value = redisTemplate.opsForValue().get(PENDING_REGISTER_KEY);

            if (value == null) {
                value = requestPendingRegisters();
            }

            try {
                return objectMapper.readValue(value, new TypeReference<List<Map<String, Object>>>() {});
            } catch (JsonProcessingException e) {
                log.error("대기 중인 가입 요청 목록 파싱 실패: {}", e.getMessage(), e);
                throw new CustomException(ErrorCode.JSON_FAILED);
            }
        } catch (Exception e) {
            log.error("대기 중인 가입 요청 목록 조회 실패", e);
            throw new CustomException(ErrorCode.GET_PENDING_REQUESTS_FAILED);
        }
    }

    private String requestPendingRegisters() throws InterruptedException {
        // 요청 식별자 생성
        String requestId = UUID.randomUUID().toString();

        // 요청 등록
        CountDownLatch latch = requestTracker.registerRequest(requestId);

        try {
            // 요청 메시지 발행
            log.debug("대기 중인 가입 요청 목록 요청 발행: {}", requestId);
//            committeePublisher.publish(PENDING_REGISTER_CHANNEL, "대기 중인 가입 요청 목록 요청:" + requestId);
            eventPublisher.publishEvent(new PendingRequestEvent(PENDING_REGISTER_CHANNEL, requestId));

            // 응답 대기
            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 응답 확인
            if (!receivedInTime || !requestTracker.isRequestSuccessful(requestId)) {
                log.warn("대기 중인 가입 요청 목록 응답 타임아웃: {}", requestId);
                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            // 응답을 받았으면 Redis에서 다시 조회
            String value = redisTemplate.opsForValue().get(PENDING_REGISTER_KEY);
            if (value == null) {
                log.warn("대기 중인 가입 요청 목록 응답 후에도 값이 없음: {}", requestId);
                throw new CustomException(ErrorCode.DATA_NOT_AVAILABLE);
            }

            log.debug("대기 중인 가입 요청 목록 조회 성공");
            return value;
        } finally {
            // 완료된 요청 정리
            requestTracker.cleanupRequest(requestId);
        }
    }

    // 사용자 가입 상태 조회
    public Map<String, Object> findRegisterUserStatus(String requestId) {
        try {
            String key = REGISTER_STATUS_KEY_PREFIX + requestId;
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                value = requestRegisterUserStatus(requestId);
            }

            try {
                return objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                log.error("사용자 가입 상태 파싱 실패: {}", e.getMessage(), e);
                throw new CustomException(ErrorCode.JSON_FAILED);
            }
        } catch (Exception e) {
            log.error("사용자 가입 상태 조회 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.GET_USER_STATUS_FAILED);
        }
    }

    private String requestRegisterUserStatus(String userRequestId) throws InterruptedException {
        // 요청 식별자 생성
        String requestId = UUID.randomUUID().toString();

        // 요청 등록
        CountDownLatch latch = requestTracker.registerRequest(requestId);

        try {
            // 이벤트 발행 (내부)
            eventPublisher.publishEvent(new UserStatusRequestEvent(REGISTER_STATUS_CHANNEL, requestId, userRequestId));

            log.debug("사용자 가입 상태 요청 발행: {}, 대상 ID: {}", requestId, userRequestId);
            // Redis 메시지 발행은 UserStatusRequestEventListener에서 처리

            // 응답 대기
            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 응답 확인
            if (!receivedInTime || !requestTracker.isRequestSuccessful(requestId)) {
                log.warn("사용자 가입 상태 응답 타임아웃: {}", requestId);
                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            // 응답을 받았으면 Redis에서 다시 조회
            String key = REGISTER_STATUS_KEY_PREFIX + userRequestId;
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.warn("사용자 가입 상태 응답 후에도 값이 없음: {}", requestId);
                throw new CustomException(ErrorCode.DATA_NOT_AVAILABLE);
            }

            log.debug("사용자 가입 상태 조회 성공: {}", userRequestId);
            return value;
        } finally {
            // 완료된 요청 정리
            requestTracker.cleanupRequest(requestId);
        }
    }

}