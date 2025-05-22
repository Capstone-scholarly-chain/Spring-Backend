//package Baeksa.money.domain.streams.service;
//
//import Baeksa.money.domain.streams.service.StreamsService;
//import io.lettuce.core.api.async.RedisAsyncCommands;
//import io.lettuce.core.codec.StringCodec;
//import io.lettuce.core.output.StatusOutput;
//import io.lettuce.core.protocol.CommandArgs;
//import io.lettuce.core.protocol.CommandType;
//import io.lettuce.core.protocol.CommandKeyword;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.data.redis.connection.stream.*;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
//import org.springframework.data.redis.stream.StreamListener;
//import org.springframework.data.redis.stream.StreamMessageListenerContainer;
//import org.springframework.data.redis.stream.Subscription;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.Duration;
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class StreamsConsumer {
////        implements StreamListener<String, MapRecord<String, Object, Object>>,
////        InitializingBean, DisposableBean {
//
//
////    private final StreamMessageListenerContainer<String,
////            MapRecord<String, Object, Object>> listenerContainer;
////    private final Subscription subscription;
//    private final StringRedisTemplate stringRedisTemplate;
//    private final RedisTemplate<String, String> redisTemplate;
//    private final StreamClaudeService streamsClaudeService;
//
//    // 요청 스트림과 응답 스트림
//    private final String REQUEST_STREAM = "spring-to-nest-stream";
//    private final String RESPONSE_STREAM = "nest-to-spring-stream";
//
//    private static final String STREAM_KEY = "RESPONSE_STREAM";
//    private static final String GROUP_NAME = "spring-consumer-group";
//    private static final String CONSUMER_NAME = "consumer-1";
//
///// 클로드
//    @Scheduled(fixedDelay = 10000)
//    public void pollResponseStream() {
//        List<MapRecord<String, Object, Object>> messages = stringRedisTemplate
//                .opsForStream()
//                .read(Consumer.from("group", "consumer"),
//                        StreamReadOptions.empty().count(10).block(Duration.ofSeconds(2)),
//                        StreamOffset.create(REQUEST_STREAM, ReadOffset.lastConsumed()));
//
//
//        if (messages == null) return;
//
//        for (MapRecord<String, Object, Object> record : messages) {
//            try {
//                String requestId = (String) record.getValue().get("requestId");
//                String dtoType = (String) record.getValue().get("DtoType");
//                String payload = (String) record.getValue().get("payload");
//                String status = (String) record.getValue().get("status"); // "SUCCESS, FAIL
//
//                log.info("📩 Received from RESPONSE_STREAM: id={}, dtoType={}, status={}, payload={}", requestId, dtoType, status, payload);
//
//                if ("SUCCESS".equalsIgnoreCase(status)) {
//                    streamsClaudeService.handleAck(requestId);
////                } else if ("DONE".equalsIgnoreCase(status)) {
////                    streamsClaudeService.handleResponse(requestId, payload);
//                } else if ("FAIL".equalsIgnoreCase(status)) {
//                    streamsClaudeService.handleFailure(requestId, payload);
//                }
//
//                // 메시지 처리 완료 후 ACK
//                /////////////////////////////////Stream앞에 바꿀것
//                stringRedisTemplate.opsForStream().acknowledge(REQUEST_STREAM, GROUP_NAME, record.getId());
//
//            } catch (Exception e) {
//                log.error("❌ Failed to process RESPONSE_STREAM record", e);
//            }
//        }
//    }
//}
//
