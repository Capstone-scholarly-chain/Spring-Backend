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

//    private String streamKey;
//    private String consumerGroupName;
//    private String consumerName;

    private final RedisOperator redisOperator;
    private final ObjectMapper objectMapper;

    private final String streamKey = "stream-test";
    // 요청 스트림과 응답 스트림
    private final String REQUEST_STREAM = "spring-to-nest-stream";
    private final String RESPONSE_STREAM = "nest-to-spring-stream";

    private static final String RESPONSE_GROUP_NAME = "nest-response-group";
    private static final String CONSUMER_NAME = "nest-consumer";

    private static final Map<String, Class<?>> dtoTypeMap = Map.of(
            "StreamTestDto", StreamReqDto.StreamTestDto.class
//            "UserRequest", UserRequest.class,
//            "PaymentEvent", PaymentEvent.class
    );

    private void handleDto(String type, Object dto) {
        switch (type) {
            case "StreamTestDto" -> {
                StreamReqDto.StreamTestDto DTO = (StreamReqDto.StreamTestDto) dto;
                log.info("DTO id: {}", DTO.getRequestId());
                log.info("DTO message: {}", DTO.getMessage());
            }
//            case "PaymentEvent" -> {
//                PaymentEvent payment = (PaymentEvent) dto;
//                log.info("💳 PaymentEvent received: {}", payment.getPaymentId());
//            }
            default -> log.warn("⚠️ No handler for type: {}", type);
        }
    }

    //dto를 다루는 핸들러를 만들지
    //하나의 뼈대dto를 상속할지 고민(근데 이건 하위 dto는 매핑이 안되는듯함)
    //자동 타입 역직렬화인 @JsonTypeInfo방식
    @Override
    public void onMessage(MapRecord<String, String, String> message) {

        Map<String, String> value = message.getValue();

//        // 더미 메시지 스킵
//        if ("true".equals(value.get("init"))) {
//            log.debug("🔧 Skipping init message: {}", message.getId());
//            // ACK만 하고 리턴
//            this.redisOperator.ackStream(RESPONSE_GROUP_NAME, message);
//            return;
//        }
        log.info("[ message ]: {}", message);
        log.info("[ Stream ]: {}", message.getStream());
        log.info("[ recordId ]: {}", message.getId());
        log.info("[ messageValue ]: {}", message.getValue());

        String DtoType = value.get("DtoType");
        String payloadJson = value.get("payload");

        Class<?> dtoClass = dtoTypeMap.get(DtoType);
        if (dtoClass == null) {
            log.warn("❓ Unknown DTO type: {}", DtoType);
            return;
        }

        try {
            Object dto = objectMapper.readValue(payloadJson, dtoClass);
            handleDto(DtoType, dto);
        } catch (Exception e) {
            log.error("❌ Failed to deserialize payload for type: {}", DtoType, e);
        }

        // 이후, ack stream
        /// //////////////////////아래 나중에 바꿀것
        this.redisOperator.ackStream(RESPONSE_GROUP_NAME, message);
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
        this.redisOperator.createStreamConsumerGroup(streamKey, RESPONSE_GROUP_NAME);

        // StreamMessageListenerContainer 설정
        this.listenerContainer = this.redisOperator.createStreamMessageListenerContainer();

        //Subscription 설정
        this.subscription = this.listenerContainer.receive(
                Consumer.from(this.RESPONSE_GROUP_NAME, CONSUMER_NAME), //누가 읽을지
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                //어떤 스트림을 어디서부터 읽을지
                this
        );

        log.info("streamKey {}: {}", streamKey);

        // 2초 마다, 정보 GET
        this.subscription.await(Duration.ofSeconds(2));

        // redis listen 시작
        this.listenerContainer.start();
    }
}
