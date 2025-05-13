package Baeksa.money.domain.ledger;

import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.RedisDto;
import Baeksa.money.global.redis.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class LedgerSubService implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 핸들러 맵: 채널 이름 → 처리 로직
    private final Map<String, Consumer<Map<String, Object>>> handlerMap = new HashMap<>();
    private final LedgerService ledgerService;

    public LedgerSubService(ObjectMapper objectMapper,
                            RedisTemplate<String, Object> redisTemplate, LedgerService ledgerService) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.ledgerService = ledgerService;

        /// //메세지는 onMessage로 하나의 메세지리스너로 읽지만, 처리를 할때는 핸들러를 나눠서 각 채널을 처리하는게 맞음
//        // 여기서 채널별 로직을 등록
//        handlerMap.put("nestjs:response:deposit:created", this::handleDepositCreated);

        handlerMap.put("nestjs:response:deposit:created", this::handleDepositCreated);
        handlerMap.put("nestjs:response:deposit:updated", this::handleDepositUpdated);
        handlerMap.put("nestjs:response:deposit:approved", this::handleDepositApproved);
        handlerMap.put("nestjs:response:deposit:rejected", this::handleDepositRejected);

        handlerMap.put("nestjs:response:withdraw:created", this::handleWithdrawCreated);
        handlerMap.put("nestjs:response:withdraw:updated", this::handleWithdrawUpdated);
        handlerMap.put("nestjs:response:withdraw:vote-update", this::handleWithdrawApproved);
        handlerMap.put("nestjs:response:withdraw:result", this::handleWithdrawRejected);
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
//                // eventType을 통한 대체 채널 식별 시도
//                String eventType = (String) messageMap.get("eventType");
//                if (eventType != null) {
//                    log.info("채널 대신 eventType으로 핸들러 식별 시도: {}", eventType);
//                    handler = getHandlerByEventType(eventType);
//                    if (handler != null) {
//                        handler.accept(messageMap);
//                        return;
//                    }
//                }
                throw new CustomException(ErrorCode.NO_CHANNEL);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }
//    // eventType을 기반으로 핸들러 반환
//    private Consumer<Map<String, Object>> getHandlerByEventType(String eventType) {
//        switch (eventType) {
//            case "TEST_PUBLISHER_REQUEST":
//                return this::handleTest;
//            // 다른 이벤트 타입에 대한 핸들러 매핑 추가
//            default:
//                return null;
//        }
//    }

    /// ///////////////////////////////////

    private void handleDepositCreated(Map<String, Object> msg) {
        log.info("입금 생성: {}", msg);

    }

    private void handleDepositUpdated(Map<String, Object> msg) {
        log.info("입금 업데이트: {}", msg);

    }

    // 승인 처리 로직
    private void handleDepositApproved(Map<String, Object> msg) {
        log.info("입금 승인: {}", msg);

    }

    // 거절 처리 로직
    private void handleDepositRejected(Map<String, Object> msg) {
        log.info("입금 거절: {}", msg);

    }




    /// ////////////////////////////////
    private void handleWithdrawCreated(Map<String, Object> msg) {
        log.info("출금 생성: {}", msg);

    }

    private void handleWithdrawUpdated(Map<String, Object> msg) {
        log.info("출금 업데이트: {}", msg);

    }

    private void handleWithdrawApproved(Map<String, Object> msg) {
        log.info("출금 승인: {}", msg);

    }

    private void handleWithdrawRejected(Map<String, Object> msg) {
        log.info("출금 거절: {}", msg);

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
