package Baeksa.money.domain.student.service;

import Baeksa.money.domain.student.event.RedisResponseEvent;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.RedisDto;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.*;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentSubscriber implements MessageListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    // 핸들러 맵: 채널 이름 → 처리 로직
    private final Map<String, Consumer<Map<String, Object>>> handlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 이벤트 핸들러 등록
        handlerMap.put("nestjs:response:register-user", this::handleRegisterUser);
        handlerMap.put("nestjs:response:membership:updated", this::handleMembershipUpdated);
        handlerMap.put("nestjs:response:membership:approve", this::handleMemberShipApprove);
        handlerMap.put("nestjs:response:membership:rejected", this::handleMembershipReject);
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
                throw new CustomException(ErrorCode.NO_CHANNEL);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }

//    private List<String> handleRegisterUser(Map<String, Object> msg) {
//        List<String> students = studentPubSubService.getStudents();
//        log.info("학생 가입 요청");
//        return students;
//    }
    private void handleRegisterUser(Map<String, Object> msg) {
        log.info("학생 가입 요청 응답 수신: {}", msg);
        // 이벤트 발행하여 필요한 서비스에서 처리하도록 함
        eventPublisher.publishEvent(new RedisResponseEvent("nestjs:response:register-user", msg));
    }

    private void handleMembershipUpdated(Map<String, Object> msg) {
        log.info("학생 요청 상태 업데이트 응답 수신: {}", msg);
        eventPublisher.publishEvent(new RedisResponseEvent("nestjs:response:membership:updated", msg));
    }

    private void handleMemberShipApprove(Map<String, Object> msg) {
        log.info("학생 요청 승인 응답 수신: {}", msg);
        eventPublisher.publishEvent(new RedisResponseEvent("nestjs:response:membership:approve", msg));
    }

    private void handleMembershipReject(Map<String, Object> msg) {
        log.info("학생 요청 거절 응답 수신: {}", msg);
        eventPublisher.publishEvent(new RedisResponseEvent("nestjs:response:membership:rejected", msg));
    }

//    public Map<String, Object> getLedgerEntryId(String ledgerEntryId) {
//        Set<String> keys = redisTemplate.keys("*" + ledgerEntryId + "*");
//        List<Map<String, Object>> result = new ArrayList<>();
//        for (String key : keys) {
//            String value;
//            try {
//                value = redisTemplate.opsForValue().get(key);
//            } catch (Exception e) {
//                throw new CustomException(ErrorCode.MY_HISTORY_FETCH_FAILED);
//            }
//            try {
//                Map<String, Object> map = objectMapper.readValue(
//                        value,
//                        new TypeReference<Map<String, Object>>() {}
//                );
//                result.add(map);
//            } catch (JsonProcessingException e) {
//                log.warn("JSON 파싱 실패. key: {}, value: {}", key, value);
//                throw new CustomException(ErrorCode.JSON_FAILED);
//            }
//        }
//        return result;
//    }

//    public Map<String, Object> getLedgerEntryById(String ledgerEntryId) {
//        String value;
//        try {
//            value = redisTemplate.opsForValue().get(ledgerEntryId);
//        } catch (Exception e) {
//            throw new CustomException(ErrorCode.MY_HISTORY_FETCH_FAILED);
//        }
//
//        try {
//            return objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {});
//        } catch (JsonProcessingException e) {
//            log.warn("JSON 파싱 실패. key: {}, value: {}", ledgerEntryId, value);
//            throw new CustomException(ErrorCode.JSON_FAILED);
//        }
//    }

//    public Map<String, String> getLedgerEntryById(String ledgerEntryId) {
//        String value;
//        try {
//            value = redisTemplate.opsForValue().get(ledgerEntryId); //LEDGER_타임스탬프_
//        } catch (Exception e) {
//            throw new CustomException(ErrorCode.MY_HISTORY_FETCH_FAILED);
//        }
//
//        try {
//            return objectMapper.readValue(value, new TypeReference<Map<String, String>>() {});
//        } catch (JsonProcessingException e) {
//            log.warn("JSON 파싱 실패. key: {}, value: {}", ledgerEntryId, value);
//            throw new CustomException(ErrorCode.JSON_FAILED);
//        }
//    }
}
