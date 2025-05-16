package Baeksa.money.domain.register.service;

import Baeksa.money.domain.committee.service.RequestResponseTracker;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RequestResponseTracker requestTracker;

    private static final String PENDING_REGISTER_KEY = "membership:pending-requests";
    private static final String PENDING_REGISTER_CHANNEL = "spring:request:pending-register";
    private static final String REGISTER_STATUS_KEY_PREFIX = "membership:request:";
    private static final String REGISTER_STATUS_CHANNEL = "spring:request:register-user-status";
    private static final int REQUEST_TIMEOUT_SECONDS = 3;
/// ///////레지스터에 3초 조회 기다리는거

    // 대기 중인 가입 요청 조회
    public List<Map<String, Object>> findPendingRequests() {
        try {
            String value = redisTemplate.opsForValue().get(PENDING_REGISTER_KEY);   //지금은 캐싱을 해서 여길 실행

            if (value == null) {
                value = requestPendingRegisters();  //값을 넣기 전에는 여기로 넘어갔음
            }

            try {
                log.info("캐시된 값: {}", value);
                // [object Object] 형태인 경우 처리
                if (value.contains("[object Object]")) {
                    log.warn("캐시된 값이 잘못된 형식입니다: {}", value);
                    return new ArrayList<>();
                }
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
            // Redis에 직접 메시지 발행
            // JSON 형식으로 메시지 구성
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("requestId", requestId);

            String message = null;
            try {
                message = objectMapper.writeValueAsString(messageMap);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            // Redis에 JSON 메시지 발행
            redisTemplate.convertAndSend(PENDING_REGISTER_CHANNEL, message);
            log.debug("대기 중인 가입 요청 목록 요청 발행: {}", requestId);
//            String message = requestId;
//            redisTemplate.convertAndSend(PENDING_REGISTER_CHANNEL, message);
//            log.debug("대기 중인 가입 요청 목록 요청 발행: {}", requestId);

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
            String value = null;
            try {
                value = redisTemplate.opsForValue().get(PENDING_REGISTER_KEY);
                log.info("value: {}", value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (value == null) {
                log.warn("대기 중인 가입 요청 목록 응답 후에도 값이 없음: {}", requestId);
                throw new CustomException(ErrorCode.DATA_NOT_AVAILABLE);    //우리 이제 값을 받음
            }

            log.debug("대기 중인 가입 요청 목록 조회 성공");
            return value;
        } finally {
            // 완료된 요청 정리
            requestTracker.cleanupRequest(requestId);
        }
    }

    // 사용자 가입 상태 조회
    public Map<String, Object> findRegisterUserStatus(String userRequestId) {
        try {
            String key = REGISTER_STATUS_KEY_PREFIX + userRequestId;
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                value = requestRegisterUserStatus(userRequestId);
            }

            try {
                log.info("캐시된 값: {}", value);
                // [object Object] 형태인 경우 처리
                if (value.contains("[object Object]")) {
                    log.warn("캐시된 값이 잘못된 형식입니다: {}", value);
                    return new HashMap<>();
                }
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
    //

    private String requestRegisterUserStatus(String userRequestId) throws InterruptedException {
        // 요청 식별자 생성
        String requestId = UUID.randomUUID().toString();

        // 요청 등록
        CountDownLatch latch = requestTracker.registerRequest(requestId);

        try {
            // 요청 객체 생성
            Map<String, String> requestData = new HashMap<>();
            requestData.put("requestId", requestId);

            // JSON 변환 및 Redis에 메시지 발행
            String message = null;
            message = objectMapper.writeValueAsString(requestData);

            redisTemplate.convertAndSend(REGISTER_STATUS_CHANNEL, message);
            log.debug("사용자 가입 상태 요청 발행: {}, 대상 ID: {}", requestId, userRequestId);

            // 응답 대기
            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 응답 확인
            if (!receivedInTime) {
                log.warn("time: {}", receivedInTime);
                if(!requestTracker.isRequestSuccessful(requestId)) log.warn("대기 중인 가입 요청 목록 응답 타임아웃: {}", requestId);
                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            // 응답을 받았으면 Redis에서 다시 조회
            String key = REGISTER_STATUS_KEY_PREFIX + requestId;
            String value = redisTemplate.opsForValue().get(key);
            log.info("value: {}", value);

            if (value == null) {
                log.warn("사용자 가입 상태 응답 후에도 값이 없음: {}", requestId);
                throw new CustomException(ErrorCode.DATA_NOT_AVAILABLE);
            }

            log.info("사용자 가입 상태 조회 성공: {}", userRequestId);
            return value;
        } catch (JsonProcessingException e) {
            log.error("사용자 가입 상태 요청 JSON 변환 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.JSON_FAILED);
        } finally {
            // 완료된 요청 정리
            requestTracker.cleanupRequest(requestId);
        }
    }
}
