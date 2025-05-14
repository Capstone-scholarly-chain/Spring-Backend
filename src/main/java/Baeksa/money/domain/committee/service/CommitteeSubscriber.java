package Baeksa.money.domain.committee.service;

//import Baeksa.money.domain.committee.eventResponses.CommitteeCountResponseEvent;
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
public class CommitteeSubscriber {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Integer> redisTemplateInteger;
    private final ObjectMapper objectMapper;
    private final RequestResponseTracker requestTracker;
    private final Map<String, Consumer<Map<String, Object>>> handlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 학생 수 응답
        handlerMap.put("nestjs:response:student-council-count", this::handleCommitteeCountResponse);
    }

    @EventListener
    public void handleCommitteeCountResponse(Map<String, Object> eventData) {
        GetResponseEvent event = objectMapper.convertValue(eventData, GetResponseEvent.class);
        log.info("학생회 수 응답 이벤트 수신: 채널={}, requestId={}", event.getChannel(), event.getRequestId());

        if (event.getRequestId() != null) {
            requestTracker.markRequestCompleted(event.getRequestId());
        }
    }


}