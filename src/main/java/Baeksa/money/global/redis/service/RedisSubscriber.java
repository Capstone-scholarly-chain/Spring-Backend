package Baeksa.money.global.redis.service;

import Baeksa.money.global.redis.RedisDto;
import Baeksa.money.global.redis.eventResponses.GetResponseEvent;
import Baeksa.money.global.redis.eventResponses.OriginResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.connection.MessageListener;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
//@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisMessageListenerContainer listenerContainer;

    // 구독할 채널 목록
    private final List<String> CHANNELS = Arrays.asList(
            "nestjs:response:register-user",
            "nestjs:response:membership:updated",
            "nestjs:response:membership:approve",
            "nestjs:response:membership:rejected",
            "nestjs:response:student-count",
            "nestjs:response:student-council-count",
            "nestjs:response:pending-register",
            "nestjs:response:status-register",
            "nestjs:response:pending-deposits",
            "nestjs:response:pending-withdraws",
            "nestjs:response:withdraw-vote-status",
            "nestjs:response:thema-balance",
            "nestjs:response:thema-balances"
    );

    public RedisSubscriber(
            ObjectMapper objectMapper,
            RedisTemplate<String, String> redisTemplate,
            ApplicationEventPublisher eventPublisher,
            RedisMessageListenerContainer listenerContainer) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
        this.listenerContainer = listenerContainer;
    }


    @PostConstruct
    public void setupSubscriptions() {
        log.info("Redis 채널 구독 설정 시작...");

        for (String channel : CHANNELS) {
            listenerContainer.addMessageListener(this, new ChannelTopic(channel));
            log.info("채널 '{}' 구독 완료", channel);
        }

        log.info("Redis 채널 구독 설정 완료 - 총 {} 개의 채널 구독 중", CHANNELS.size());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
            String publishMessage = new String(message.getBody(), StandardCharsets.UTF_8);

            log.info("Redis 메시지 수신 - 채널: {}, 메시지: {}", channel, publishMessage);

            // 메시지 파싱
            Map<String, Object> messageData;
            try {
                // JSON 파싱 시도
                messageData = objectMapper.readValue(publishMessage, Map.class);
                log.info("[ messageData1 ] : {}", messageData);
                //publishMessage는 채널과 id
            } catch (JsonProcessingException e) {
                try {
                    // MessageDto 형식으로 파싱 시도
                    RedisDto.MessageDto redisDto = objectMapper.readValue(publishMessage, RedisDto.MessageDto.class);
                    //redisDto 채널 / 메세지에 채널&메세지

                    // 메시지의 채널 정보가 있으면 사용
                    if (redisDto.getChannel() != null && !redisDto.getChannel().isEmpty()) {
                        channel = redisDto.getChannel();
                        log.info("[ channel ] : {}", channel);
                    }
                    //채널과 메세지
                    messageData = redisDto.getMessage();
                    log.info("[ messageData2 ] : {}", messageData);//requestId만 들어가는듯
                } catch (JsonProcessingException e2) {
                    // 단순 문자열 메시지 처리
                    messageData = new HashMap<>();
                    messageData.put("rawMessage", publishMessage);
                    log.info("messageData3: {}", messageData);

                    // 요청 ID 추출 시도
                    String requestId = extractRequestIdFromMessage(publishMessage);
                    if (requestId != null) {
                        messageData.put("requestId", requestId);
                        log.info("[ requestId1 ]: {}", requestId);
                    }
                }
            }

            // 요청 ID 추출
            String requestId = extractRequestId(messageData, publishMessage);
            log.info("[ requestId2 ]: {}", requestId);

            OriginResponseEvent payload;
            OriginResponseEvent.Data data;


            // 채널별 이벤트 발행
            switch (channel) {
                case "nestjs:response:register-user":
                    payload = objectMapper.convertValue(messageData, OriginResponseEvent.class);
                    data = payload.getData();
                    log.info(" [ data ]: id={}, 상태={}", data.getId(), data.getStatus());
                    eventPublisher.publishEvent(new OriginResponseEvent("nestjs:response:register-user", data));
                    break;
                case "nestjs:response:membership:approve":
                    payload = objectMapper.convertValue(messageData, OriginResponseEvent.class);
                    data = payload.getData();
                    log.info(" [ data ]: id={}, 상태={}", data.getId(), data.getStatus());
                    eventPublisher.publishEvent(new OriginResponseEvent("nestjs:response:membership:approve", data));
                    break;
                case "nestjs:response:membership:rejected":
                    payload = objectMapper.convertValue(messageData, OriginResponseEvent.class);
                    data = payload.getData();
                    log.info(" [ data ]: id={}, 상태={}", data.getId(), data.getStatus());
                    eventPublisher.publishEvent(new OriginResponseEvent("nestjs:response:membership:rejected", data));
                    break;
                case "nestjs:response:student-count":
                    eventPublisher.publishEvent(new GetResponseEvent("nestjs:response:student-count", requestId));
                    break;
                case "nestjs:response:student-council-count":
                    eventPublisher.publishEvent(new GetResponseEvent("nestjs:response:student-council-count", requestId));
                    break;
                case "nestjs:response:pending-register":
                    log.info("채널 '{}' 메시지에 대한 이벤트 발행 완료2222", channel);
                    eventPublisher.publishEvent(new GetResponseEvent("nestjs:response:pending-register", requestId));
                    break;
                case "nestjs:response:status-register":
                    eventPublisher.publishEvent(new GetResponseEvent("nestjs:response:status-register", requestId));
                    break;
                case "nestjs:response:pending-deposits":
                    eventPublisher.publishEvent(new GetResponseEvent("nestjs:response:pending-deposits", requestId));
                    break;
                case "nestjs:response:pending-withdraws":
                    eventPublisher.publishEvent(new GetResponseEvent("nestjs:response:pending-withdraws", requestId));
                    break;
                case "nestjs:response:withdraw-vote-status":
                    eventPublisher.publishEvent(new GetResponseEvent("nestjs:response:withdraw-vote-status", requestId));
                    break;
                case "nestjs:response:thema-balance":
                    eventPublisher.publishEvent(new GetResponseEvent("nestjs:response:thema-balance", requestId));
                    break;
                case "nestjs:response:thema-balances":
                    eventPublisher.publishEvent(new GetResponseEvent("nestjs:response:thema-balances", requestId));
                    break;
            }

            log.info("채널 '{}' 메시지에 대한 이벤트 발행 완료", channel);
        } catch (Exception e) {
            log.error("메시지 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 메시지 데이터에서 요청 ID 추출
     */
    private String extractRequestId(Map<String, Object> data, String rawMessage) {
        // Map에서 요청 ID 추출 시도
        if (data != null && data.containsKey("requestId")) {
            return String.valueOf(data.get("requestId"));
        }

        // 원본 메시지에서 요청 ID 추출 시도
        return extractRequestIdFromMessage(rawMessage);
    }

    /**
     * 원시 메시지에서 요청 ID 추출
     */
    private String extractRequestIdFromMessage(String message) {
        if (message == null) return null;

        if (message.matches("[a-f0-9\\-]{36}")) {
            return message;
        }
        log.info("message: {}", message);
        return null;
    }
}


