package Baeksa.money.domain.ledger;

import Baeksa.money.global.redis.RedisDto;
import Baeksa.money.global.redis.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
        handlerMap.put("nestjs:response:withdraw:updated", this::handleWithdrawApproved);
        handlerMap.put("nestjs:response:withdraw:result", this::handleWithdrawRejected);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {
//            String publishMessage = redisTemplate.getStringSerializer().deserialize(message.getBody());
            //이건 onMessage의 파라미터 message야
            String publishMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            RedisDto.MessageDto redisDto = objectMapper.readValue(publishMessage, RedisDto.MessageDto.class);

            String channel = redisDto.getChannel();
            Map<String, Object> messages = redisDto.getMessage();

            log.info("Redis Subscribe Channel : {}", channel);
            log.info("Redis SUB Message : {}", publishMessage);

            // 채널별 처리 핸들러 실행
            Consumer<Map<String, Object>> handler = handlerMap.get(channel);
            if (handler != null) {
                handler.accept(messages);
            } else {
                log.warn("등록되지 않은 채널입니다: {}", channel);
            }
        }

        catch (JsonProcessingException e){
            log.error("JSON 파싱 오류: {}", e.getMessage());
        }
        catch (Exception e) {
            log.error("Redis 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }


    /// ///////////////////////////////////

    private void handleDepositCreated(Map<String, Object> msg) {
        log.info("입금 생성");

    }

    private void handleDepositUpdated(Map<String, Object> msg) {
        log.info("입금 업데이트");

    }

    // 승인 처리 로직
    private void handleDepositApproved(Map<String, Object> msg) {
        log.info("입금 승인");

    }

    // 거절 처리 로직
    private void handleDepositRejected(Map<String, Object> msg) {
        log.info("입금 거절");

    }




    /// ////////////////////////////////
    private void handleWithdrawCreated(Map<String, Object> msg) {
        log.info("출금 생성");

    }

    private void handleWithdrawUpdated(Map<String, Object> msg) {
        log.info("출금 업데이트");

    }

    private void handleWithdrawApproved(Map<String, Object> msg) {
        log.info("출금 승인");

    }

    private void handleWithdrawRejected(Map<String, Object> msg) {
        log.info("출금 거절");

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
