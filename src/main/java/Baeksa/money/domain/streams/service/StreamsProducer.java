//package Baeksa.money.domain.streams.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.stream.RecordId;
//import org.springframework.data.redis.connection.stream.StreamRecords;
//import org.springframework.data.redis.connection.stream.StringRecord;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class StreamsProducer {
//
////    private final RedisTemplate<String, String> redisTemplate;
////    @Qualifier("customStringRedisTemplate")
//    private final StringRedisTemplate stringRedisTemplate;
//    private final ObjectMapper objectMapper;
////    private final String streamKey = "stream-test";
////    private final String REQUEST_STREAM = "spring-to-nest-stream";
//

//
//    // StreamsProducer 클래스의 초기화 메소드
//    @PostConstruct
//    public void init() {
//        try {
//            // 스트림이 존재하는지 확인
//            Boolean exists = stringRedisTemplate.hasKey(SPRING_TO_NESTJS_STREAM);
//            log.info("Stream {} exists: {}", SPRING_TO_NESTJS_STREAM, exists);
//
//            // 필요한 경우 초기 메시지 추가하여 스트림 생성
//            if (Boolean.FALSE.equals(exists)) {
//                Map<String, String> initialData = new HashMap<>();
//                initialData.put("init", "true");
//                StringRecord record = StreamRecords.string(initialData).withStreamKey(SPRING_TO_NESTJS_STREAM);
//                RecordId recordId = stringRedisTemplate.opsForStream().add(record);
//                log.info("Created stream {} with initial record: {}", SPRING_TO_NESTJS_STREAM, recordId);
//            }
//        } catch (Exception e) {
//            log.error("Failed to initialize stream: {}", e.getMessage(), e);
//        }
//    }
//
////    public RecordId testAddMessage(Object requestData) {
////
////        String requestId = UUID.randomUUID().toString();
////
////        try {
////            log.info("🚀 Processing client request with ID: {}", requestId);
////
////            // 요청 데이터 준비
////            // recordId는 스트림 내부id이고 nest.js는 알 수 없음.
////            // requestId의 경우 응답 매칭을 위한 추적 ID임. nest.js가 알 수 있음
////            Map<String, String> data = new HashMap<>();
//////            data.put("requestId", requestId);
////            data.put("DtoType", requestData.getClass().getName());
////            data.put("payload", objectMapper.writeValueAsString(requestData));
//////            data.put("timestamp", String.valueOf(System.currentTimeMillis()));
//////            data.put("status", Status.SUCCESS.name()); //테스트용, 나중엔 보낼때 뺴야됨
////            log.info("data: {}", data);
////
////
////            // Redis Stream에 요청 메시지 발행
////            StringRecord record = StreamRecords.string(data).withStreamKey(SPRING_TO_NESTJS_STREAM);
////            RecordId recordId = stringRedisTemplate.opsForStream().add(record);
////            log.info("✅ Request sent to Nest.js via stream: {}, ID: {}", requestId, recordId);
////            return recordId;
////        } catch (Exception e) {
////            log.error("❌ Failed to add message to stream: {}", e.getMessage(), e);
////            throw new RuntimeException("Failed to add message to Redis Stream: " + e.getMessage(), e);
////        }
////    }
////
////
////    public RecordId publishSignup2(String message) {
////        try {
////            Map<String, Object> map = new HashMap<>();
////            map.put("message", message);
////            String json = objectMapper.writeValueAsString(map);
////
////            Map<String, String> data = new HashMap<>();
////            data.put("payload", json);
////            log.info("✅ data: {}", data);
////
////            return addMessage(SPRING_TO_NESTJS_STREAM, data);
////
////        } catch (JsonProcessingException e) {
////            log.error("❌ Failed to serialize block", e);
////            throw new RuntimeException(e);
////        }
////    }
////
////    public RecordId addMessage(String streamKey, Map<String, String> data){
////        try {
////            log.info("✅ Adding record to stream: {}, data: {}", streamKey, data);
////            StringRecord record = StreamRecords.string(data).withStreamKey(streamKey);
////            RecordId recordId = stringRedisTemplate.opsForStream().add(record);
////            log.info("✅ recordId: {}", recordId);
////            return recordId;
////        } catch (Exception e) {
////            log.error("❌ Failed to add message to stream: {}", e.getMessage(), e);
////            throw new RuntimeException("Failed to add message to Redis Stream: " + e.getMessage(), e);
////        }
////    }
//}
