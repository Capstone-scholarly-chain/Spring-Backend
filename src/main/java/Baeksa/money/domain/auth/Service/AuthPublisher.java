package Baeksa.money.domain.auth.Service;

import Baeksa.money.domain.auth.enums.Role;
import Baeksa.money.domain.committee.service.RequestResponseTracker;
import Baeksa.money.domain.student.event.LedgerRequestEvent;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RequestResponseTracker requestTracker;

    private static final String REGISTER_USER_CHANNEL = "spring:request:register-user";
    private static final int REQUEST_TIMEOUT_SECONDS = 3;

    public void publish(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }

    public Map<String, Object> publishSignup(String studentId, String username, Role role) {

        // 요청 ID 생성
        String uuid = UUID.randomUUID().toString();

        // CountDownLatch 생성 및 등록
        CountDownLatch latch = requestTracker.registerRequest(uuid);

        // 메시지 생성
        Map<String, Object> map = new HashMap<>();
        map.put("userId", studentId);
        map.put("name", username);
        map.put("orgType", role);
        map.put("uuid", uuid);  // requestId 포함

        try {
            // 메시지 직렬화
            String message = objectMapper.writeValueAsString(map);

            // 메시지 발행
            redisTemplate.convertAndSend(REGISTER_USER_CHANNEL, message);
            log.info("회원가입 요청 발행: uuid={}, userId={}", uuid, studentId);

            // 응답 대기 (타임아웃 설정)
            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 타임아웃 확인
            if (!receivedInTime) {
                log.warn("회원가입 요청 응답 타임아웃: uuid={}", uuid);
                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            // 요청 성공 여부 확인
            if (!requestTracker.isRequestSuccessful(uuid)) {
                log.warn("회원가입 요청 실패: uuid={}", uuid);
                throw new CustomException(ErrorCode.REQUEST_FAILED);
            }

            // 성공적으로 응답 받음
            log.info("회원가입 요청 성공: requestId={}", uuid);
            return map;

        } catch (InterruptedException e) {
            log.error("회원가입 요청 중단: ", e);
            throw new CustomException(ErrorCode.REQUEST_INTERRUPTED);
        } catch (JsonProcessingException e) {
            log.error("메시지 직렬화 오류: ", e);
            throw new CustomException(ErrorCode.JSON_FAILED);
        } finally {
            // 요청 정리
            requestTracker.cleanupRequest(uuid);
        }
    }
}
