package Baeksa.money.global.redis.service;

import Baeksa.money.global.redis.RedisDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final RedisService redisService;

    // 핸들러 맵: 채널 이름 → 처리 로직
    private final Map<String, Consumer<Map<String, Object>>> handlerMap = new HashMap<>();

    public RedisSubscriber(ObjectMapper objectMapper,
                           RedisService redisService,
                           RedisTemplate<String, Object> redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisService = redisService;
        this.redisTemplate = redisTemplate;

        // 여기서 채널별 로직을 등록
        handlerMap.put("spring:request:register-user", this::handleRegisterUser);
        handlerMap.put("spring:request:membership", this::handleMembership);
        handlerMap.put("spring:request:approve", this::handleApprove);
        handlerMap.put("spring:request:ledger", this::handleLedger);
        handlerMap.put("spring:request:approve-withdraw", this::handleApproveWithdraw);
        handlerMap.put("spring:request:reject-withdraw", this::handleRejectWithdraw);
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

            // 💡 채널별 처리 핸들러 실행
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

    private void handleRegisterUser(Map<String, Object> msg) {
        log.info("회원가입 메시지 처리");
        handleCommonCaching(msg);
    }

    private void handleMembership(Map<String, Object> msg) {
        log.info("학생/학생회 가입 신청 메시지 처리");
        handleCommonCaching(msg);
    }

    private void handleApprove(Map<String, Object> msg) {
        log.info("학생회 가입 승인");
        handleCommonCaching(msg);
    }

    private void handleLedger(Map<String, Object> msg) {
        log.info("학생 입금 기입/학생회 출금 기입/학생회 입금 승인");
        handleCommonCaching(msg);
    }

    // 승인 처리 로직
    private void handleApproveWithdraw(Map<String, Object> msg) {
        log.info("학생이 출금 승인");
        handleCommonCaching(msg);
    }

    // 거절 처리 로직
    private void handleRejectWithdraw(Map<String, Object> msg) {
        log.info("학생이 출금 거부");
        handleCommonCaching(msg);
    }

    // 공통 캐싱 처리 함수
    private void handleCommonCaching(Map<String, Object> msg) {
        String id = (String) msg.get("Id"); //id, requestId, entryId, ledgerEntryId -> Id로 통합하자요
        if (id == null) {
            log.error("수신된 메시지에 Id 필드가 없습니다.");
            return;
        }
        log.info("수신된 ID: {}", id);

        String[] parts = redisService.getParts(id);
        if (parts == null) {
            log.error("수신된 메시지에 studentId 필드가 없습니다.");
            return;
        }

        String theme = parts[0];
        String studentId = parts[2];
        String redisKey = theme + "_" + studentId;

        Duration ttl = Duration.ofHours(4);
        redisTemplate.opsForValue().set(redisKey, id, ttl);

        log.info("Redis 캐싱 완료: {} → {}", redisKey, id);
    }

//    @Override
//    public void onMessage(Message message, byte[] pattern) {
//
//        try {
//    //            String publishMessage = redisTemplate.getStringSerializer().deserialize(message.getBody());
//            //이건 onMessage의 파라미터 message야
//            String publishMessage = new String(message.getBody(), StandardCharsets.UTF_8);
//            RedisDto.MessageDto redisDto = objectMapper.readValue(publishMessage, RedisDto.MessageDto.class);
//
//            log.info("Redis Subscribe Channel : " + redisDto.getChannel());
//            log.info("Redis SUB Message : {}", publishMessage);
//
//            Map<String, Object> messages = redisDto.getMessage();
//            String id = (String) messages.get("id");
//
//            log.info("수신된 ID: {}", id);
//
//            // theme, studentId 추출
//            String[] parts = redisService.getParts(id);
//            if (parts == null) return;
//
//            String theme = parts[0];
//            String studentId = parts[2];
//
//            // 캐싱
//            String redisKey = theme + "_" + studentId;
//            Duration ttl = Duration.ofMinutes(4);   //4시간동안 유지
//            redisTemplate.opsForValue().set(redisKey, id, ttl);
//
//            log.info("Redis 캐싱 완료: {} → {}", redisKey, id);
//        }
//        catch (JsonProcessingException e){
//            log.error("JSON 파싱 오류: {}", e.getMessage());
//        }
//        catch (Exception e) {
//            log.error("Redis 메시지 처리 실패: {}", e.getMessage(), e);
//        }
//    }
}

