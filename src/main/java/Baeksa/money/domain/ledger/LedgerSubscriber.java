package Baeksa.money.domain.ledger;

import Baeksa.money.domain.student.event.RedisResponseEvent;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Service
@Slf4j
public class LedgerSubscriber implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    // 핸들러 맵: 채널 이름 → 처리 로직
    private final Map<String, Consumer<Map<String, Object>>> handlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 입금 응답 구독 
        handlerMap.put("nestjs:response:deposit:created", this::handleDepositCreated);
        handlerMap.put("nestjs:response:deposit:updated", this::handleDepositUpdated);
        handlerMap.put("nestjs:response:deposit:approved", this::handleDepositApproved);
        handlerMap.put("nestjs:response:deposit:rejected", this::handleDepositRejected);

        // 출금 응답 구독
        handlerMap.put("nestjs:response:withdraw:created", this::handleWithdrawCreated);
        handlerMap.put("nestjs:response:withdraw:updated", this::handleWithdrawUpdated);
        handlerMap.put("nestjs:response:withdraw:voted", this::handleWithdrawVoted);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {
//            String publishMessage = redisTemplate.getStringSerializer().deserialize(message.getBody());
            //이건 onMessage의 파라미터 message야, pattern으로 채널 뽑을 수 있음
            String publishMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            String channel = new String(pattern, StandardCharsets.UTF_8);

            log.info("Redis 메시지 수신 - 채널: {}, 메시지: {}", channel, publishMessage);

            // JSON 메시지를 Map으로 변환
            Map<String, Object> messageMap = objectMapper.readValue(publishMessage,
                    new TypeReference<Map<String, Object>>() {});

            // 채널별 처리 핸들러 실행
            Consumer<Map<String, Object>> handler = handlerMap.get(channel);
            if (handler != null) {
                handler.accept(messageMap);
            } else {
                throw new CustomException(ErrorCode.NO_CHANNEL);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }


    private void handleDepositCreated(Map<String, Object> msg) {
        log.info("입금 생성: {}", msg);
        eventPublisher.publishEvent(new RedisResponseEvent("nestjs:response:deposit:created", msg));

    }

    private void handleDepositUpdated(Map<String, Object> msg) {
        log.info("입금 업데이트: {}", msg);
        eventPublisher.publishEvent(new RedisResponseEvent("nestjs:response:deposit:updated", msg));
    }

    // 승인 처리 로직
    private void handleDepositApproved(Map<String, Object> msg) {
        log.info("입금 승인: {}", msg);
        eventPublisher.publishEvent(new RedisResponseEvent("nestjs:response:deposit:approved", msg));
    }

    // 거절 처리 로직
    private void handleDepositRejected(Map<String, Object> msg) {
        log.info("입금 거절: {}", msg);
        eventPublisher.publishEvent(new RedisResponseEvent("nestjs:response:deposit:rejected", msg));
    }




    /// ////////////////////////////////
    private void handleWithdrawCreated(Map<String, Object> msg) {
        log.info("출금 생성: {}", msg);
        eventPublisher.publishEvent(new RedisResponseEvent("nestjs:response:withdraw:created", msg));
    }

    private void handleWithdrawUpdated(Map<String, Object> msg) {
        log.info("출금 업데이트: {}", msg);
        eventPublisher.publishEvent(new RedisResponseEvent("nestjs:response:withdraw:updated", msg));
    }


    private void handleWithdrawVoted(Map<String, Object> msg) {
        log.info("출금 투표 신청 응답: {}", msg);
        eventPublisher.publishEvent(new RedisResponseEvent("nestjs:response:withdraw:voted", msg));
    }


    private String handleLedgerEntryId(Map<String, Object> msg) {
        String id = (String) msg.get("ledgerEntryId");
        if (id == null) {
            log.error("수신된 메시지에 ledgerEntryId 필드가 없습니다.");
        }
        log.info("수신된 ID: {}", id);
        return id;
    }
}


