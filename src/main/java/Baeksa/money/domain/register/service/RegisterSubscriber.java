package Baeksa.money.domain.register.service;

//import Baeksa.money.domain.committee.event2.StudentCountResponseEvent;
import Baeksa.money.domain.committee.service.RequestResponseTracker;
import Baeksa.money.domain.register.eventResponses.RegisterApproveResponse;
import Baeksa.money.domain.register.eventResponses.RegisterRejectResponse;
import Baeksa.money.domain.register.eventResponses.RegisterResponseEvent;
import Baeksa.money.global.redis.eventResponses.GetResponseEvent;
import Baeksa.money.global.redis.eventResponses.OriginResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterSubscriber {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Integer> redisTemplateInteger;
    private final ObjectMapper objectMapper;
    private final RequestResponseTracker requestTracker;

    // Origin 타입 이벤트를 처리하는 핸들러맵
    private final Map<String, Consumer<OriginResponseEvent>> originHandlerMap = new HashMap<>();

    // Get 타입 이벤트를 처리하는 핸들러맵
    private final Map<String, Consumer<GetResponseEvent>> getHandlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 등록 승인 거절 - Origin 타입 이벤트 처리
        originHandlerMap.put("nestjs:response:register-user", this::handleRegisterResponse);
        originHandlerMap.put("nestjs:response:membership:approve", this::handleRegisterApproveResponse);
        originHandlerMap.put("nestjs:response:membership:rejected", this::handleRegisterRejectResponse);

        // 조회 - Get 타입 이벤트 처리
        getHandlerMap.put("nestjs:response:pending-register", this::handlePendingRegisterResponse);
        getHandlerMap.put("nestjs:response:status-register", this::handleRegisterStatus);
    }

    @EventListener
    public void handleRegisterResponse(OriginResponseEvent eventData) {
        log.info("회원 가입 && 조직 가입 신청 완료 이벤트 수신: 데이터={}", eventData);

        OriginResponseEvent payload = objectMapper.convertValue(eventData, OriginResponseEvent.class);
        OriginResponseEvent.Data data = payload.getData();

        log.info(" [ 가입 응답 수신 ]: id={}, 상태={}", data.getId(), data.getStatus());
        if (data.getId() != null) {
            // 요청 완료 처리
            requestTracker.markRequestCompleted(data.getId());
        }
    }

//    @EventListener
//    public void handleRegisterResponse(Map<String, Object> eventData) {
//        log.info("회원 가입 && 조직 가입 신청 완료 이벤트 수신: 데이터={}", eventData);
//
//        OriginResponseEvent payload = objectMapper.convertValue(eventData, OriginResponseEvent.class);
//        OriginResponseEvent.Data data = payload.getData();
//
//        log.info(" [ 가입 응답 수신 ]: id={}, 상태={}", data.getId(), data.getStatus());
//        if (data.getId() != null) {
//            // 요청 완료 처리
//            requestTracker.markRequestCompleted(data.getId());
//        }
//    }

    @EventListener
    public void handleRegisterApproveResponse(OriginResponseEvent eventData) {
        log.info("가입 승인 이벤트 수신: 데이터={}", eventData);

        OriginResponseEvent payload = objectMapper.convertValue(eventData, OriginResponseEvent.class);
        OriginResponseEvent.Data data = payload.getData();

        log.info(" [ 가입 승인 응답 수신 ]: id={}, 상태={}", data.getId(), data.getStatus());
        if (data.getId() != null) {
            // 요청 완료 처리
            requestTracker.markRequestCompleted(data.getId());
        }
    }

    @EventListener
    public void handleRegisterRejectResponse(OriginResponseEvent eventData) {
        log.info("가입 거절 이벤트 수신: 데이터={}", eventData);

        OriginResponseEvent payload = objectMapper.convertValue(eventData, OriginResponseEvent.class);
        OriginResponseEvent.Data data = payload.getData();

        log.info(" [ 가입 거절 응답 수신 ]: id={}, 상태={}", data.getId(), data.getStatus());
        if (data.getId() != null) {
            // 요청 완료 처리
            requestTracker.markRequestCompleted(data.getId());
        }
    }

    @EventListener
    public void handlePendingRegisterResponse(GetResponseEvent eventData) {
        log.info("팬딩중인 등록 신청 이벤트 원본 데이터: {}", eventData);
        // Map을 GetResponseEvent로 변환
        try {
            // Map을 GetResponseEvent로 변환
            GetResponseEvent event = objectMapper.convertValue(eventData, GetResponseEvent.class);
            log.info("팬딩중인 등록 신청 이벤트 변환 성공: 채널={}, requestId={}", event.getChannel(), event.getRequestId());

            if (event.getRequestId() != null) {
                log.info("요청 완료 표시: requestId={}", event.getRequestId());
                requestTracker.markRequestCompleted(event.getRequestId());
            } else {
                log.warn("requestId가 null입니다");
            }
        } catch (Exception e) {
            log.error("이벤트 데이터 변환 중 오류: {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleRegisterStatus(GetResponseEvent eventData) {
        // Map을 GetResponseEvent로 변환
        GetResponseEvent event = objectMapper.convertValue(eventData, GetResponseEvent.class);

        log.info("한 요청 조회 이벤트 수신: 채널={}, requestId={}", event.getChannel(), event.getRequestId());

        if (event.getRequestId() != null) {
            requestTracker.markRequestCompleted(event.getRequestId());
        }
    }
}
