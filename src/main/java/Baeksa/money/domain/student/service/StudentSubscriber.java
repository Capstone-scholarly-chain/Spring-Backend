package Baeksa.money.domain.student.service;

import Baeksa.money.domain.committee.service.RequestResponseTracker;
import Baeksa.money.domain.student.event.RedisResponseEvent;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.RedisDto;
import Baeksa.money.global.redis.eventResponses.GetResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentSubscriber {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Integer> redisTemplateInteger;
    private final ObjectMapper objectMapper;
    private final RequestResponseTracker requestTracker;

    private final Map<String, Consumer<Map<String, Object>>> handlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 학생 수 응답
        handlerMap.put("nestjs:response:student-count", this::handleStudentCountResponse);
    }

    @EventListener
    public void handleStudentCountResponse(Map<String, Object> eventData) {
        // Map을 GetResponseEvent로 변환
        GetResponseEvent event = objectMapper.convertValue(eventData, GetResponseEvent.class);

        log.info("학생 수 응답 이벤트 수신: 채널={}, requestId={}", event.getChannel(), event.getRequestId());

        if (event.getRequestId() != null) {
            requestTracker.markRequestCompleted(event.getRequestId());
        }
    }
}
