package Baeksa.money.domain.streams.service.github;

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
    public void processPendingMessage() {
        try {
            PendingMessages pendingMessages = redisOperator
                    .findStreamPendingMessages(NESTJS_TO_SPRING_STREAM, SPRING_CONSUMER_GROUP, SPRING_CONSUMER_NAME);
            log.info("📋 Found {} pending messages", pendingMessages.size());

            if (pendingMessages.isEmpty()) {
                log.debug("✅ No pending messages to process");
                return;
            }

            for (PendingMessage pendingMessage : pendingMessages) {
                // 다른 컨슈머가 처리하던 메시지를 이 컨슈머가 가져옴
                redisOperator.claimStream(pendingMessage, SPRING_CONSUMER_NAME);

                try {
                    // Stream 메시지 조회
                    MapRecord<String, String, String> messageToProcess =
                            redisOperator.findStreamMessageById(NESTJS_TO_SPRING_STREAM, pendingMessage.getIdAsString());

                    if (messageToProcess == null) {
                        log.warn("❗ 메시지를 찾을 수 없음: {}", pendingMessage.getIdAsString());
                        continue;
                    }

                    // 실제 메시지 처리 로직
                    log.info("🔧 Processing message: {}", messageToProcess.getValue());

                    // 처리 완료 후 ack
                    redisOperator.ackStream(NESTJS_CONSUMER_GROUP, messageToProcess);
                    log.info("✅ Acked message: {}", pendingMessage.getIdAsString());

                } catch (Exception e) {
                    log.error("❌ 메시지 처리 중 예외 발생 - ID: {}", pendingMessage.getIdAsString(), e);

                    // 에러 횟수 누적 (예: 5회 이상이면 로그만 남기고 넘기기)
                    redisOperator.increaseRedisValue("errorCount", pendingMessage.getIdAsString());
                }
            }
        } catch (Exception e) {
            log.error("❌ Pending message 스케줄러 오류", e);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("✅ PendingMessageScheduler initialized");
        log.info("   - Stream: {}", NESTJS_TO_SPRING_STREAM);
        log.info("   - Consumer Group: {}", SPRING_CONSUMER_GROUP);
        log.info("   - Consumer: {}", SPRING_CONSUMER_NAME);
    }
}
