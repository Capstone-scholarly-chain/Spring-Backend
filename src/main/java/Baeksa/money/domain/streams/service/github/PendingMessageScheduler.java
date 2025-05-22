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

//    private String streamKey;
//    private String consumerGroupName;
//    private String consumerName;
    private final RedisOperator redisOperator;
    private final String streamKey = "stream-test";
    private static final String RESPONSE_GROUP_NAME = "nest-response-group";
    private static final String CONSUMER_NAME = "nest-consumer";

    @Scheduled(fixedRate = 10000)   //10초마다 작동
    public void processPendingMessage() {
        try {
            PendingMessages pendingMessages = this.redisOperator
                    .findStreamPendingMessages(streamKey, RESPONSE_GROUP_NAME, CONSUMER_NAME);
            log.info("📋 Found {} pending messages", pendingMessages.size());

            if (pendingMessages.isEmpty()) {
                log.debug("✅ No pending messages to process");
                return;
            }

            for (PendingMessage pendingMessage : pendingMessages) {
                this.redisOperator.claimStream(pendingMessage, CONSUMER_NAME);
//                processSinglePendingMessage(pendingMessage);
            }
        } catch (Exception e) {
            log.error("❌ Error in pending message scheduler", e);
        }
    }

//        for(PendingMessage pendingMessage : pendingMessages) {
//            // claimStream()을 통해 다른 컨슈머가 처리하던 메시지를 현재 컨슈머로 가져옴
//            this.redisOperator.claimStream(pendingMessage, CONSUMER_NAME);
//            try {
//                // Stream message 조회
//                MapRecord<String, String, String> messageToProcess = this.redisOperator
//                        .findStreamMessageById(this.streamKey, pendingMessage.getIdAsString());
//                if (messageToProcess == null) {
//                    log.info("존재하지 않는 메시지");
//                } else {
//                    // 해당 메시지 에러 발생 횟수 확인
//                    int errorCount = (int) this.redisOperator
//                            .getRedisValue("errorCount", pendingMessage.getIdAsString());
//
//                    // 에러 5회이상 발생
//                    if (errorCount >= 5) {
//                        log.info("재 처리 최대 시도 횟수 초과");
//                    }
//                    // 두개 이상의 consumer에게 delivered 된 메시지
//                    else if (pendingMessage.getTotalDeliveryCount() >= 2) {
//                        log.info("최대 delivery 횟수 초과");
//                    } else {
//                        // 처리할 로직 구현 ex) service.someServiceMethod();
//                    }
//                    // ack stream
//                    this.redisOperator.ackStream(RESPONSE_GROUP_NAME, messageToProcess);
//                }
//            } catch (Exception e) {
//                // 해당 메시지 에러 발생 횟수 + 1
//                this.redisOperator.increaseRedisValue("errorCount", pendingMessage.getIdAsString());
//            }
//        }
//    }



    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("✅ PendingMessageScheduler initialized");
        log.info("   - Stream: {}", streamKey);
        log.info("   - Consumer Group: {}", RESPONSE_GROUP_NAME);
        log.info("   - Consumer: {}", CONSUMER_NAME);
    }
    //    private final String streamKey = "stream-test";
    //    // 요청 스트림과 응답 스트림
    //    private final String REQUEST_STREAM = "spring-to-nest-stream";
    //    private final String RESPONSE_STREAM = "nest-to-spring-stream";
    //
    //    private static final String RESPONSE_GROUP_NAME = "spring-response-group";
    //    private static final String CONSUMER_NAME = "spring-consumer";
}
