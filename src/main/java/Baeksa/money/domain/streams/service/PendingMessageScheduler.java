package Baeksa.money.domain.streams.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@EnableScheduling
@Component
@RequiredArgsConstructor
public class PendingMessageScheduler implements InitializingBean {

    private final RedisOperator redisOperator;
    //    private final String streamKey = "spring-nestjs-requests";
//    private static final String RESPONSE_GROUP_NAME = "nest-consumer-group";
//    private static final String CONSUMER_NAME = "nest-consumer";
    // 🔥 NestJS와 통합된 설정 사용
    private static final String SPRING_TO_NESTJS_STREAM = "spring-nestjs-requests";
    private static final String NESTJS_TO_SPRING_STREAM = "nestjs-spring-responses";
    private static final String NESTJS_CONSUMER_GROUP = "nest-consumer-group";    // NestJS와 동일
    private static final String NESTJS_CONSUMER_NAME = "nest-consumer";           // NestJS와 동일
    private static final String SPRING_CONSUMER_GROUP = "spring-consumer-group";  // Spring 전용
    private static final String SPRING_CONSUMER_NAME = "spring-consumer";

    @Scheduled(fixedRate = 10000)   // 10초마다 작동
    public void processPendingMessages() {
        log.debug("🔍 Pending 메시지 스케줄러 실행 중...");

        // 1. Spring → NestJS 요청의 Pending 체크
//        checkSpringToNestJsPendingMessages();

        // 2. NestJS → Spring 응답의 Pending 체크
        checkNestJsToSpringPendingMessages();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("✅ PendingMessageScheduler initialized");
        log.info("  요청 스트림 모니터링: {} (그룹: {})", SPRING_TO_NESTJS_STREAM, NESTJS_CONSUMER_GROUP);
        log.info("  응답 스트림 처리: {} (그룹: {})", NESTJS_TO_SPRING_STREAM, SPRING_CONSUMER_GROUP);
    }

    /**
     * 🔥 NestJS가 보낸 응답이 Spring에서 처리되지 않은 경우 체크 및 처리
     */
    private void checkNestJsToSpringPendingMessages() {
        try {
            PendingMessages pendingMessages = redisOperator
                    .findStreamPendingMessages(NESTJS_TO_SPRING_STREAM, SPRING_CONSUMER_GROUP, SPRING_CONSUMER_NAME);

            log.debug("📋 NestJS → Spring 응답 중 {} 개의 pending 메시지 발견", pendingMessages.size());

            if (pendingMessages.isEmpty()) {
                return;
            }

            for (PendingMessage pendingMessage : pendingMessages) {
                try {
                    // 다른 컨슈머가 처리하던 메시지를 이 컨슈머가 가져옴
                    redisOperator.claimStream(pendingMessage, SPRING_CONSUMER_NAME);

                    // Stream 메시지 조회
                    MapRecord<String, String, String> messageToProcess =
                            redisOperator.findStreamMessageById(NESTJS_TO_SPRING_STREAM, pendingMessage.getIdAsString());

                    if (messageToProcess == null) {
                        log.warn("❗ 응답 메시지를 찾을 수 없음: {}", pendingMessage.getIdAsString());
                        continue;
                    }

                    // 실제 응답 메시지 처리 (RedisStreamConsumer와 동일한 로직)
                    log.info("🔧 Pending 응답 메시지 처리: ID={}", pendingMessage.getIdAsString());
                    processResponseMessage(messageToProcess);

                    // 올바른 컨슈머 그룹으로 ACK (Spring 그룹!)
                    redisOperator.ackStream(SPRING_CONSUMER_GROUP, messageToProcess);
                    log.info("✅ 응답 메시지 ACK 완료: {}", pendingMessage.getIdAsString());

                } catch (Exception e) {
                    log.error("❌ 응답 pending 메시지 처리 중 예외 발생 - ID: {}", pendingMessage.getIdAsString(), e);

                    // 에러 횟수 누적
                    long errorCount = redisOperator.increaseRedisValue("errorCount", pendingMessage.getIdAsString());
                    if (errorCount > 5) {
                        log.warn("🚨 응답 메시지 {} 에러 횟수 초과 ({}회), 강제 ACK 처리",
                                pendingMessage.getIdAsString(), errorCount);

                        // 강제 ACK로 무한 루프 방지
                        MapRecord<String, String, String> errorMessage =
                                redisOperator.findStreamMessageById(NESTJS_TO_SPRING_STREAM, pendingMessage.getIdAsString());
                        if (errorMessage != null) {
                            redisOperator.ackStream(SPRING_CONSUMER_GROUP, errorMessage);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ NestJS → Spring pending 체크 오류", e);
        }
    }

    // 공통 응답 메시지 처리
    private void processResponseMessage(MapRecord<String, String, String> message) {
        try {
            var messageData = message.getValue();

            String originalRecordId = messageData.get("originalRecordId");
            String requestType = messageData.get("requestType");
            String success = messageData.get("success");
            String result = messageData.get("result");

            log.info("📨 Pending 응답 처리: Type={}, Success={}, OriginalId={}",
                    requestType, success, originalRecordId);

            // 간단한 로깅만 (상세 처리는 RedisStreamConsumer에서)
            if ("true".equals(success)) {
                log.info("✅ 지연된 성공 응답 처리됨: {}", requestType);
            } else {
                log.warn("❌ 지연된 실패 응답 처리됨: Type={}, Error={}", requestType, result);
            }

        } catch (Exception e) {
            log.error("❌ 응답 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }
}

    /**
     * Spring이 보낸 요청이 NestJS에서 처리되지 않은 경우 체크
     * 사실 재전송을 할 것이 아니기에 의미는 없고 단순 체크용
     */
//    private void checkSpringToNestJsPendingMessages() {
//        try {
//            PendingMessages pendingMessages = redisOperator
//                    .findStreamPendingMessages(SPRING_TO_NESTJS_STREAM, NESTJS_CONSUMER_GROUP, NESTJS_CONSUMER_NAME);
//
//            if (!pendingMessages.isEmpty()) {
//                log.warn("Spring → NestJS 요청 중 {} 개의 pending 메시지 발견", pendingMessages.size());
//
//                for (PendingMessage pendingMessage : pendingMessages) {
//                    try {
//                        // 메시지 조회
//                        MapRecord<String, String, String> messageToProcess =
//                                redisOperator.findStreamMessageById(SPRING_TO_NESTJS_STREAM, pendingMessage.getIdAsString());
//
//                        if (messageToProcess == null) {
//                            log.warn("요청 메시지를 찾을 수 없음: {}", pendingMessage.getIdAsString());
//                            continue;
//                        }
//
//                        // 오래된 메시지 체크 (예: 1분 이상 pending)
//                        long messageAge = System.currentTimeMillis() - pendingMessage.getElapsedTimeSinceLastDelivery().toMillis();
//                        if (messageAge > 60000) { // 1분 이상
//                            log.error("요청 메시지가 {}ms 동안 처리되지 않음: ID={}, Data={}",
//                                    messageAge, pendingMessage.getIdAsString(), messageToProcess.getValue());
//
////                            // 재시도 또는 알림 로직
////                            handleStuckRequest(messageToProcess, pendingMessage);
//                        }
//
//                    } catch (Exception e) {
//                        log.error("요청 pending 메시지 처리 중 오류 - ID: {}", pendingMessage.getIdAsString(), e);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Spring → NestJS pending 체크 오류", e);
//        }
//    }
//}
