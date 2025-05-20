package Baeksa.money.domain.streams;

import Baeksa.money.domain.auth.enums.Role;
import Baeksa.money.domain.streams.dto.StreamReqDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreamsProducer {

//    private final RedisTemplate<String, String> redisTemplate;
//    @Qualifier("customStringRedisTemplate")
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final String streamKey = "stream-test";

    // StreamsProducer 클래스의 초기화 메소드
    @PostConstruct
    public void init() {
        try {
            // 스트림이 존재하는지 확인
            Boolean exists = stringRedisTemplate.hasKey(streamKey);
            log.info("Stream {} exists: {}", streamKey, exists);

            // 필요한 경우 초기 메시지 추가하여 스트림 생성
            if (Boolean.FALSE.equals(exists)) {
                Map<String, String> initialData = new HashMap<>();
                initialData.put("init", "true");
                StringRecord record = StreamRecords.string(initialData).withStreamKey(streamKey);
                RecordId recordId = stringRedisTemplate.opsForStream().add(record);
                log.info("Created stream {} with initial record: {}", streamKey, recordId);
            }
        } catch (Exception e) {
            log.error("Failed to initialize stream: {}", e.getMessage(), e);
        }
    }

    public RecordId testAddMessage() {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("test", "value");

            StringRecord record = StreamRecords.string(data).withStreamKey(streamKey);
            RecordId recordId = stringRedisTemplate.opsForStream().add(record);
            log.info("✅ recordId: {}", recordId);
            return recordId;
        } catch (Exception e) {
            log.error("❌ Failed to add message to stream: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add message to Redis Stream: " + e.getMessage(), e);
        }
    }

    public RecordId publishSignup2(String message) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("message", message);
            String json = objectMapper.writeValueAsString(map);

            Map<String, String> data = new HashMap<>();
            data.put("payload", json);
            log.info("✅ data: {}", data);

            return addMessage(streamKey, data);

        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize block", e);
            throw new RuntimeException(e);
        }
    }

    public RecordId addMessage(String streamKey, Map<String, String> data){
        try {
            log.info("✅ Adding record to stream: {}, data: {}", streamKey, data);
            StringRecord record = StreamRecords.string(data).withStreamKey(streamKey);
            RecordId recordId = stringRedisTemplate.opsForStream().add(record);
            log.info("✅ recordId: {}", recordId);
            return recordId;
        } catch (Exception e) {
            log.error("❌ Failed to add message to stream: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add message to Redis Stream: " + e.getMessage(), e);
        }
    }


//    public RecordId publishSignup(String studentId, String username, Role role){
//        try {
//            Map<String, Object> map = new HashMap<>();
//            map.put("studentId", studentId);
//            map.put("username", username);
//            map.put("role", role);
//            String json = objectMapper.writeValueAsString(map);
//
//            Map<String, String> data = new HashMap<>();
//            data.put("payload", json);
//
//            //둘은 같은 구문인듯
//            StringRecord record = StreamRecords.string(data).withStreamKey(streamKey);
////            MapRecord<Object, String, String> mapRecord = StreamRecords.mapBacked(data).withStreamKey(streamKey);
//            log.info("✅ record: {}", record);
////            log.info("✅ mapRecord: {}", mapRecord);
//
//            //XADD - 메시지가 redis에 성공적으로 저장되면 고유한 recordId를 반환함 (예: "1716208550440-0")
//            //recordId는 추후에 XACK, XREAD, XDEL 메시지 조회와 삭제에 사용됨
//            RecordId recordId = stringRedisTemplate.opsForStream().add(record);
//            log.info("✅ recordId: {}", recordId);
//            return recordId;
//
//        } catch (JsonProcessingException e) {
//            log.error("❌ Failed to serialize block", e);
//            throw new RuntimeException(e);
//        }
//    }


}
