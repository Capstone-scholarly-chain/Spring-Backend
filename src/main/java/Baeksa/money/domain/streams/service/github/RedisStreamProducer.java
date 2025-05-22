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
    private final String streamKey = "stream-test";
    private final String REQUEST_STREAM = "spring-to-nest-stream";
    private static final String RESPONSE_GROUP_NAME = "nest-response-group";
    private static final String CONSUMER_NAME = "nest-consumer";



    public RecordId sendMessage(Object requestData) {
        try {
            log.info("🚀 Processing sendMessage");
            /// String, String에 유의하기 !!!!
            Map<String, String> data = new HashMap<>();
            data.put("DtoType", requestData.getClass().getName());
            data.put("payload", objectMapper.writeValueAsString(requestData));
            log.info(" [ data ] : {}", data);

            return addMessage(streamKey, data);

        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize block", e);
            throw new RuntimeException(e);
        }
    }

    public RecordId addMessage(String streamKey, Map<String, String> data){
        try {
            StringRecord record = StreamRecords.string(data).withStreamKey(streamKey);
            RecordId recordId = stringRedisTemplate.opsForStream().add(record);
            log.info("✅ Request sent to Nest.js via stream: {}", recordId);
            return recordId;
        } catch (Exception e) {
            log.error("❌ Failed to add message to stream: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.STREAMS_SEND_FAIL);
        }
    }
}
