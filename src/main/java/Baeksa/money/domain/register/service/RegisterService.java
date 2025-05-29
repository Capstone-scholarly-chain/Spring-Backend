package Baeksa.money.domain.register.service;

import Baeksa.money.global.redis.service.RequestResponseTracker;
import Baeksa.money.domain.streams.service.RedisStreamProducer;
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
    private final RedisStreamProducer redisStreamProducer;

    private static final String PENDING_REGISTER_KEY = "membership:pending-requests";
    private static final String PENDING_REGISTER_CHANNEL = "spring:request:pending-register";
    private static final String REGISTER_STATUS_KEY_PREFIX = "membership:request:";
    private static final String REGISTER_STATUS_CHANNEL = "spring:request:register-user-status";
    private static final int REQUEST_TIMEOUT_SECONDS = 3;
/// ///////레지스터에 3초 조회 기다리는거

    // 대기 중인 가입 요청 조회
public List<Map<String, Object>> getPendingRequests() {
    try {
        // 🔥 Redis에서 JSON 문자열로 저장된 데이터 조회
        String cachedData = redisTemplate.opsForValue().get(PENDING_REGISTER_KEY);
        log.info("이건읽냐");

        if (cachedData != null && !cachedData.isEmpty()) {
            try {
                // JSON 문자열을 List<Map>으로 파싱
                List<Map<String, Object>> result = objectMapper.readValue(
                        cachedData,
                        new TypeReference<List<Map<String, Object>>>() {}
                );
                log.info("캐시에서 대기중인 입금 요청 조회: {} 건", result.size());
                return result;
            } catch (Exception e) {
                log.warn("캐시된 입금 요청 데이터 파싱 실패, 재요청: {}", e.getMessage());
                redisTemplate.delete(PENDING_REGISTER_KEY); // 잘못된 데이터 삭제
            }
        }

        // 캐시에 없거나 파싱 실패 시 NestJS에 요청
        log.info("캐시에 대기중인 입금 요청 없음, NestJS에 요청");
        return requestPendingRequests();

    } catch (Exception e) {
        log.error("대기중인 입금 항목 조회 실패", e);
        throw new CustomException(ErrorCode.PENDING_DEPOSIT_FETCH_FAILED);
    }
}

    private List<Map<String, Object>> requestPendingRequests() throws InterruptedException, JsonProcessingException {
        String recordId = redisStreamProducer.sendMessage("대기중인 입금 요청", "GET_PENDING_REQUESTS").toString();
        CountDownLatch latch = requestTracker.registerRequest(recordId);

        try {
            log.info("대기중인 입금 요청 발송: recordId={}", recordId);   //여기까지 로그
            //nest가 보낸 거 받는데, 파싱 실패

            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!receivedInTime) {
                log.warn("대기중인 입금 요청 응답 타임아웃: recordId={}", recordId);
//                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            if (!requestTracker.isRequestSuccessful(recordId)) {
                log.warn("대기중인 입금 요청 응답 실패: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_FAILED);
            }

            // 응답 데이터 조회
            String data = redisTemplate.opsForValue().get(PENDING_REGISTER_KEY);

            if (data == null) {
                log.warn("대기중인 입금 요청 응답 후에도 데이터 없음: recordId={}", recordId);
                return new ArrayList<>(); // 빈 리스트 반환
            }

            // JSON 문자열을 List<Map>으로 파싱
            return objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {});

        } finally {
            requestTracker.cleanupRequest(recordId);
        }
    }


    // 사용자 가입 상태 조회
    public String findRegisterUserStatus(String userRequestId) {
        try {
            String key = REGISTER_STATUS_KEY_PREFIX + userRequestId;
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                value = requestRegisterUserStatus(userRequestId);
            }
            return value;
        } catch (Exception e) {
            log.error("사용자 가입 상태 조회 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.GET_USER_STATUS_FAILED);
        }
    }
    //

    private String requestRegisterUserStatus(String userRequestId) throws InterruptedException {
        String recordId = redisStreamProducer.sendMessageRequestId(userRequestId, "GET_REQUEST_STATUS").toString();
        CountDownLatch latch = requestTracker.registerRequest(recordId);

        try {
            log.debug("사용자 가입 상태 요청 발행: {}, 대상 ID: {}", recordId, userRequestId);

            // 응답 대기
            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!receivedInTime) {
                log.warn("대기중인 입금 요청 응답 타임아웃: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            if (!requestTracker.isRequestSuccessful(recordId)) {
                log.warn("대기중인 입금 요청 응답 실패: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_FAILED);
            }

            // 응답을 받았으면 Redis에서 다시 조회
            String key = REGISTER_STATUS_KEY_PREFIX + userRequestId;
            log.info("key: {}", key);
            String value = redisTemplate.opsForValue().get(key);
            log.info("value: {}", value);

            if (value == null) {
                log.warn("사용자 가입 상태 응답 후에도 값이 없음: {}", userRequestId);
                throw new CustomException(ErrorCode.DATA_NOT_AVAILABLE);    //데이터 없어서 터진듯
            }

            log.info("사용자 가입 상태 조회 성공: {}", userRequestId);
            return value;
//            return objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
        } finally {
            // 완료된 요청 정리
            requestTracker.cleanupRequest(recordId);
        }
    }
}
