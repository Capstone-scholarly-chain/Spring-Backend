//package Baeksa.money.domain.streams.service.newStream;
//
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.stream.MapRecord;
//import org.springframework.data.redis.connection.stream.ReadOffset;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.stream.StreamMessageListenerContainer;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class StreamService {
//
//    private final StringRedisTemplate stringRedisTemplate;
//    private final ObjectMapper objectMapper;
//
//    // 요청 스트림과 응답 스트림
//    private final String REQUEST_STREAM = "spring-to-nest-stream";
//    private final String RESPONSE_STREAM = "nest-to-spring-stream";
//
//    private static final String RESPONSE_GROUP_NAME = "spring-response-group";
//    private static final String CONSUMER_NAME = "spring-consumer";
//
//    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
//
//    @PostConstruct
//    public void init() {
//        setupConsumerGroups();
//        startStreamListeners();
//    }
//
//    /**
//     * Consumer Group 설정
//     */
//    private void setupConsumerGroups() {
//        try {
//            createConsumerGroupIfNotExists(RESPONSE_STREAM, RESPONSE_GROUP_NAME);
//            log.info("✅ Consumer groups initialized");
//        } catch (Exception e) {
//            log.error("❌ Failed to setup consumer groups", e);
//        }
//    }
//
//    /**
//     * Consumer Group 생성 (존재하지 않을 때만)
//     */
//    private void createConsumerGroupIfNotExists(String streamKey, String groupName) {
//        try {
//            if (!stringRedisTemplate.hasKey(streamKey)) {
//                // 스트림이 없으면 더미 메시지로 생성
//                Map<String, String> dummyData = Map.of("init", "true");
//                stringRedisTemplate.opsForStream().add(streamKey, dummyData);
//            }
//
//            // Consumer Group 존재 여부 확인
//            boolean groupExists = stringRedisTemplate.opsForStream()
//                    .groups(streamKey)
//                    .stream()
//                    .anyMatch(group -> group.groupName().equals(groupName));
//
//            if (!groupExists) {
//                stringRedisTemplate.opsForStream()
//                        .createGroup(streamKey, ReadOffset.from("0"), groupName);
//                log.info("✅ Created consumer group: {} for stream: {}", groupName, streamKey);
//            }
//        } catch (Exception e) {
//            log.warn("⚠️ Consumer group might already exist: {}", e.getMessage());
//        }
//    }
//}
//
//
