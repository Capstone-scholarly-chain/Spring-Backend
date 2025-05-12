package Baeksa.money.global.redis.service;

import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.RedisDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.connection.MessageListener;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
//@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 핸들러 맵: 채널 이름 → 처리 로직
    private final Map<String, Consumer<Map<String, Object>>> handlerMap = new HashMap<>();

    public RedisSubscriber(ObjectMapper objectMapper,
                           RedisTemplate<String, Object> redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;

        // 여기서 채널별 로직을 등록
        //nestjs로 수정

        handlerMap.put("spring:request:register-user", this::handleRegisterUser);
        handlerMap.put("spring:request:membership", this::handleMembership);
        handlerMap.put("spring:request:approve", this::handleApprove);
        handlerMap.put("spring:request:ledger", this::handleLedger);
        handlerMap.put("spring:request:approve-withdraw", this::handleApproveWithdraw);
        handlerMap.put("spring:request:reject-withdraw", this::handleRejectWithdraw);
    }

    @PostConstruct
    public void init() {
        log.info("RedisSubscriber 초기화 완료. 등록된 핸들러: {}", handlerMap.keySet());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {
//            String publishMessage = redisTemplate.getStringSerializer().deserialize(message.getBody());
            //이건 onMessage의 파라미터 message야
            String publishMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            String channel = new String(pattern, StandardCharsets.UTF_8);
//            RedisDto.MessageDto redisDto = objectMapper.readValue(publishMessage, RedisDto.MessageDto.class);
//            String channel = redisDto.getChannel();
//            Map<String, Object> messages = redisDto.getMessage();

            log.info("Redis Subscribe Channel : {}", channel);
            log.info("Redis SUB Message : {}", publishMessage);

            // JSON 메시지를 Map으로 변환
            Map<String, Object> messageMap = objectMapper.readValue(publishMessage,
                    new TypeReference<Map<String, Object>>() {});

            // 채널별 처리 핸들러 실행
            Consumer<Map<String, Object>> handler = handlerMap.get(channel);
            if (handler != null) {
                handler.accept(messageMap);
            } else {
                log.warn("등록되지 않은 채널입니다: {}", channel);
            }
        }

        catch (JsonProcessingException e){
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
        catch (Exception e) {
            log.error("Redis 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }

    private void handleRegisterUser(Map<String, Object> msg) {
        log.info("회원가입 메시지 처리");

    }

    private void handleMembership(Map<String, Object> msg) {
        log.info("학생/학생회 가입 신청 메시지 처리");

    }

    private void handleApprove(Map<String, Object> msg) {
        log.info("학생회 가입 승인");

    }

    private void handleLedger(Map<String, Object> msg) {
        log.info("학생 입금 기입/학생회 출금 기입/학생회 입금 승인");

    }

    // 승인 처리 로직
    private void handleApproveWithdraw(Map<String, Object> msg) {
        log.info("학생이 출금 승인");

    }

    // 거절 처리 로직
    private void handleRejectWithdraw(Map<String, Object> msg) {
        log.info("학생이 출금 거부");

    }


    private String handleRequestId(Map<String, Object> msg) {
        String id = (String) msg.get("requestId");
        if (id == null) {
            log.error("수신된 메시지에 requestId 필드가 없습니다.");
        }
        log.info("수신된 ID: {}", id);
        return id;
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

