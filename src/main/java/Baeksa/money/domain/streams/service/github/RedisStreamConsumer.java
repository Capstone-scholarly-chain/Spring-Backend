package Baeksa.money.domain.streams.service.github;


import Baeksa.money.domain.streams.dto.StreamReqDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamConsumer implements StreamListener<String, MapRecord<String, String, String>>,
        InitializingBean, DisposableBean {
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private Subscription subscription;

    private final RedisOperator redisOperator;
    private final ObjectMapper objectMapper;


//    private final String streamKey = "spring-nestjs-requests";
//    // 요청 스트림과 응답 스트림
//    private final String REQUEST_STREAM = "spring-to-nest-stream";
//    private final String RESPONSE_STREAM = "nest-to-spring-stream";
//
//    private static final String RESPONSE_GROUP_NAME = "nest-response-group";
//    private static final String CONSUMER_NAME = "nest-consumer";

    private static final String SPRING_TO_NESTJS_STREAM = "spring-nestjs-requests";
    private static final String NESTJS_TO_SPRING_STREAM = "nestjs-spring-responses";
    private static final String NESTJS_CONSUMER_GROUP = "nest-consumer-group";    // NestJS와 동일
    private static final String NESTJS_CONSUMER_NAME = "nest-consumer";           // NestJS와 동일
    private static final String SPRING_CONSUMER_GROUP = "spring-consumer-group";  // Spring 전용
    private static final String SPRING_CONSUMER_NAME = "spring-consumer";

    @Override
    public void onMessage(MapRecord<String, String, String> message) {

        Map<String, String> value = message.getValue();

        log.info("[ Consumer 리딩중 ]:");
        log.info("[ Stream ]: {}", message.getStream());
        log.info("[ recordId ]: {}", message.getId());
        log.info("[ messageValue ]: {}", message.getValue());

        try {
            // 🔥 공통 필드 추출
            String originalRecordId = value.get("originalRecordId");
            String requestType = value.get("requestType");
            String success = value.get("success");
            String result = value.get("result");
            String processingTime = value.get("processingTime");
            String timestamp = value.get("timestamp");

            // 🔥 기본 유효성 검사
            if (success == null || requestType == null) {
                log.warn("❗ 필수 필드 누락: originalRecordId={}, requestType={}", originalRecordId, requestType);
                return;
            }

            // 🔥 성공/실패에 따른 처리
            boolean isSuccess = "true".equals(success);
            log.info("🎯 Processing response: Type={}, Success={}, OriginalId={}",
                    requestType, isSuccess, originalRecordId);

            if (isSuccess) {
                handleSuccessResponse(requestType, originalRecordId, result, processingTime, timestamp);
            } else {
                handleErrorResponse(requestType, originalRecordId, result, processingTime, timestamp);
            }

        } catch (Exception e) {
            log.error("❌ 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        } finally {
            // ACK 처리
            try {
                this.redisOperator.ackStream(SPRING_CONSUMER_GROUP, message);
                log.info("✅ ACK 완료: {}", message.getId());
            } catch (Exception e) {
                log.error("❌ ACK 실패: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 🔥 성공 응답 처리 - requestType별로 다른 로직 적용 가능
     */
    private void handleSuccessResponse(String requestType, String originalRecordId,
                                       String result, String processingTime, String timestamp) {
        log.info("[ requestType ]: {}", requestType);

        try {
            // result를 JSON으로 파싱하여 상세 데이터 추출
            Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
//
//            // NestJS에서 오는 공통 응답 구조 파싱
//            Boolean success = (Boolean) resultData.get("success");
//            String message = (String) resultData.get("message");
//            String ledgerEntryId = (String) resultData.get("ledgerEntryId");
//            String requestId = (String) resultData.get("requestId");
//            String requestStatus = (String) resultData.get("requestStatus");
//

            switch (requestType) {
                case "TEST_REQUEST" -> {
                    log.info("🧪 테스트 요청 성공");
                    log.info("   - 결과: {}", resultData);
                    log.info("   - 처리시간: {}ms", processingTime);
                    // 필요시 추가 비즈니스 로직
                }
                case "REGISTER_USER" -> {
                    log.info("👤 사용자 등록 성공");
                    log.info("   - 사용자ID: {}", resultData.get("userId"));
                    log.info("   - 상태: {}", resultData.get("status"));
                    // DB 업데이트, 알림 발송 등
                }
                case "APPROVE_MEMBERSHIP" -> {
                    log.info("✅ 멤버십 승인 완료");
                    log.info("   - 결과: {}", resultData);
                    // 승인 후처리 로직
                }
                case "REJECT_MEMBERSHIP" -> {
                    log.info("❌ 멤버십 거절 완료");
                    log.info("   - 결과: {}", resultData);
                    // 거절 후처리 로직
                }
                case "GET_STUDENT_COUNT", "GET_COUNCIL_COUNT", "GET_PENDING_REQUESTS" -> {
                    log.info("📊 조회 요청 완료: {}", requestType);
                    log.info("   - 결과: {}", resultData);
                    // 조회 결과 캐싱 등
                }
                case "GET_REQUEST_STATUS" -> {
                    log.info("📋 상태 조회 완료");
                    log.info("   - 상태: {}", resultData.get("status"));
                    // 상태 업데이트 등
                }
                default -> {
                    log.info("🔄 일반 요청 처리 완료: {}", requestType);
                    log.info("   - 결과: {}", resultData);
                }
            }

        } catch (Exception e) {
            log.warn("⚠️ result JSON 파싱 실패, 원본 그대로 사용: {}", result);
        }
    }

    private void handleErrorResponse(String requestType, String originalRecordId,
                                     String error, String processingTime, String timestamp) {
        log.error("❌ 요청 처리 실패: Type={}, Error={}", requestType, error);
        log.error("   - 원본 RecordId: {}", originalRecordId);
        log.error("   - 처리시간: {}ms", processingTime);
    }


    @Override
    public void destroy() throws Exception {
        if(this.subscription != null){
            this.subscription.cancel();
        }
        if(this.listenerContainer != null){
            this.listenerContainer .stop();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // Consumer Group 설정
        this.redisOperator.createStreamConsumerGroup(NESTJS_TO_SPRING_STREAM, SPRING_CONSUMER_GROUP);

        // StreamMessageListenerContainer 설정
        this.listenerContainer = this.redisOperator.createStreamMessageListenerContainer();

        //Subscription 설정
        this.subscription = this.listenerContainer.receive(
                Consumer.from(this.SPRING_CONSUMER_GROUP, SPRING_CONSUMER_NAME), //누가 읽을지
                StreamOffset.create(NESTJS_TO_SPRING_STREAM, ReadOffset.lastConsumed()),
                //어떤 스트림을 어디서부터 읽을지
                this
        );

        log.info("streamKey: {}", NESTJS_TO_SPRING_STREAM);

        // 2초 마다, 정보 GET
        this.subscription.await(Duration.ofSeconds(2));

        // redis listen 시작
        this.listenerContainer.start();
    }
}
