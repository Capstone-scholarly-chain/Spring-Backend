package Baeksa.money.domain.streams.service.github;

import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamProducer {


    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
//    private final String streamKey = "spring-nestjs-requests";
//    private final String REQUEST_STREAM = "spring-to-nest-stream";
//    private static final String RESPONSE_GROUP_NAME = "nest-response-group";
//    private static final String CONSUMER_NAME = "nest-consumer";

    private static final String SPRING_TO_NESTJS_STREAM = "spring-nestjs-requests";
    private static final String NESTJS_TO_SPRING_STREAM = "nestjs-spring-responses";
    private static final String NESTJS_CONSUMER_GROUP = "nest-consumer-group";    // NestJS와 동일
    private static final String NESTJS_CONSUMER_NAME = "nest-consumer";           // NestJS와 동일
    private static final String SPRING_CONSUMER_GROUP = "spring-consumer-group";  // Spring 전용
    private static final String SPRING_CONSUMER_NAME = "spring-consumer";


    public RecordId sendMessage(Object requestData) {
        try {
            log.info("🚀 Processing sendMessage");
            /// String, String에 유의하기 !!!!
            Map<String, String> data = new HashMap<>();
//            data.put("DtoType", requestData.getClass().getSimpleName());    //getName()으로 하면 패키지부터 띄움
            data.put("requestType", "TEST_REQUEST");    //getName()으로 하면 패키지부터 띄움
            data.put("data", objectMapper.writeValueAsString(requestData));
            data.put("timestamp", String.valueOf(System.currentTimeMillis()));

            log.info(" [ data ] : {}", data);

            return addMessage(SPRING_TO_NESTJS_STREAM, data);

        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize block", e);
            throw new RuntimeException(e);
        }
    }

    public RecordId addMessage(String streamKey, Map<String, String> data){
        try {
            StringRecord record = StreamRecords.string(data).withStreamKey(streamKey);
            RecordId recordId = stringRedisTemplate.opsForStream().add(record);
            log.info("✅ Request sent to Nest.js stream: {}", recordId);
            return recordId;
        } catch (Exception e) {
            log.error("❌ Failed to add message to stream: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.STREAMS_SEND_FAIL);
        }
    }
}
