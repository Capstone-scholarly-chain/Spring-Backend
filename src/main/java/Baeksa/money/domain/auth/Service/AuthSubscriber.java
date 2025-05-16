package Baeksa.money.domain.auth.Service;

import Baeksa.money.domain.committee.service.RequestResponseTracker;
import Baeksa.money.global.redis.eventResponses.GetResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthSubscriber {

    private final ObjectMapper objectMapper;
    private final RequestResponseTracker requestTracker;


    // 핸들러 맵: 채널 이름 → 처리 로직
    private final Map<String, Consumer<Map<String, Object>>> handlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 회원가입 후 등록 신청
        handlerMap.put("nestjs:response:register-user", this::handleRegisterUser);
    }

    @EventListener
    public void handleRegisterUser(Map<String, Object> eventData) {
        GetResponseEvent event = objectMapper.convertValue(eventData, GetResponseEvent.class);
        log.info("회원가입 응답 이벤트 수신: 채널={}, requestId={}", event.getChannel(), event.getRequestId());

        if (event.getRequestId() != null) {
            requestTracker.markRequestCompleted(event.getRequestId());
            log.info("회원 가입 응답 처리 완료: requestId={}", event.getRequestId());
        }
    }
}
