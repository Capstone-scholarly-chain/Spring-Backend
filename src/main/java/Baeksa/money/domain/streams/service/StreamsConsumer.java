package Baeksa.money.domain.streams.service;

import Baeksa.money.domain.streams.service.StreamsService;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.CommandKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreamsConsumer {
//        implements StreamListener<String, MapRecord<String, Object, Object>>,
//        InitializingBean, DisposableBean {


//    private final StreamMessageListenerContainer<String,
//            MapRecord<String, Object, Object>> listenerContainer;
//    private final Subscription subscription;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final StreamClaudeService streamsClaudeService;

    private static final String STREAM_KEY = "RESPONSE_STREAM";
    private static final String GROUP_NAME = "spring-consumer-group";
    private static final String CONSUMER_NAME = "consumer-1";


    @Scheduled(fixedDelay = 1000)
    public void pollResponseStream() {
        List<MapRecord<String, Object, Object>> messages = stringRedisTemplate
                .opsForStream()
                .read(Consumer.from("group", "consumer"),
                        StreamReadOptions.empty().count(10).block(Duration.ofSeconds(2)),
                        StreamOffset.create("RESPONSE_STREAM", ReadOffset.lastConsumed()));


        if (messages == null) return;

        for (MapRecord<String, Object, Object> record : messages) {
            try {
                String requestId = (String) record.getValue().get("requestId");
                String payload = (String) record.getValue().get("payload");
                String status = (String) record.getValue().get("status"); // "ACK", "FAIL", "DONE"

                log.info("📩 Received from RESPONSE_STREAM: id={}, status={}, payload={}", requestId, status, payload);

                if ("ACK".equalsIgnoreCase(status)) {
                    streamsClaudeService.handleAck(requestId);
                } else if ("DONE".equalsIgnoreCase(status)) {
                    streamsClaudeService.handleResponse(requestId, payload);
                } else if ("FAIL".equalsIgnoreCase(status)) {
                    streamsClaudeService.handleFailure(requestId, payload);
                }

                // 메시지 처리 완료 후 ACK
                stringRedisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, record.getId());

            } catch (Exception e) {
                log.error("❌ Failed to process RESPONSE_STREAM record", e);
            }
        }
    }
}


    //    private final RedisOperator redisOperator;
//    public static final String NUMBER_KEY = "number";
//    public static final String LAST_RESULT_HASH_KEY = "last_result";
//    public static final String PROCESSED_HASH_KEY = "processed";
//    public static final String ERRORS_HASH_KEY = "errors";
//    private final String oddListKey = "odd-list";
//    private final String evenListKey = "even-list";
//    private final String recordCacheKey = "record-cache";
//    private final String streamKey = "stream-test";
//    private final String consumerName = "stream-test-reader";
//    private final String consumerGroupName = "stream-test";
//
 //스케줄링을 하게 되면 메세지가 없을때도 지속적으로 redis에 접속해서 리소스 낭비
//onMessage의 경우 메시지가 오면 자동으로 콜백 방식으로 수신 처리
//    @Override
//    public void onMessage(MapRecord<String, Object, Object> message) {
//        /// //////복붙
//        //extract the number from the message
//        try {
//            String inputNumber = (String) message.getValue().get(NUMBER_KEY);
//            final int number = Integer.parseInt(inputNumber);
//            if (number % 2 == 0) {
//                redisTemplate.opsForList().rightPush(evenListKey, inputNumber);
//            } else {
//                redisTemplate.opsForList().rightPush(oddListKey, inputNumber);
//            }
//            redisTemplate.opsForHash().put(recordCacheKey, LAST_RESULT_HASH_KEY, number);
//            redisTemplate.opsForHash().increment(recordCacheKey, PROCESSED_HASH_KEY, 1);
//            redisTemplate.opsForStream().acknowledge(consumerName, message);
//            log.info("Message has been processed");
//        } catch (Exception ex) {
//            //log the exception and increment the number of errors count
//            log.error("Failed to process the message: {} ", message.getValue().get(NUMBER_KEY), ex);
//            redisTemplate.opsForHash().increment(recordCacheKey, ERRORS_HASH_KEY, 1);
//        }
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
////        //Stream 기본 정보
////        this.streamKey = "stream-test";
////        this.consumerGroupName = "stream-test";
////        this.consumerName = "stream-test-reader";
////
////        //Consumer 그룹 설정
////        this.redisOperator.createStreamConsumerGroup(streamKey, consumerGroupName);
////
////        //StreamMessageListenerContainer
////        this.listenerContainer = this.redisOperator
//
//        //name for this consumer which will be registered with consumer group
//
//
//        try {
//            //create consumer group for the stream
//            // if stream does not exist it will create stream first then create consumer group
//            if (!redisTemplate.hasKey(streamKey)) {
//                log.info("{} does not exist. Creating stream along with the consumer group", streamKey);
//                RedisAsyncCommands commands = (RedisAsyncCommands) redisTemplate.getConnectionFactory()
//                        .getConnection().getNativeConnection();
//                CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
//                        .add(CommandKeyword.CREATE)
//                        .add(streamKey)
//                        .add(consumerGroupName)
//                        .add("0")
//                        .add("MKSTREAM");
//                commands.dispatch(CommandType.XGROUP, new StatusOutput<>(StringCodec.UTF8), args);
//            } else {
//                //creating consumer group
//                redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroupName);
//            }
//        } catch (Exception ex) {
//            log.info("Consumer group already present: {}", consumerGroupName);
//        }
//
//
//        this.listenerContainer = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(),
//                StreamMessageListenerContainer
//                        .StreamMessageListenerContainerOptions.builder()
//                        .hashKeySerializer(new JdkSerializationRedisSerializer())
//                        .hashValueSerializer(new JdkSerializationRedisSerializer())
//                        .pollTimeout(Duration.ofMillis(config.getStreamPollTimeout()))
//                        .build());
//
//        this.subscription = listenerContainer.receive(
//                Consumer.from(consumerGroupName, consumerName),
//                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
//                this);
//
//        subscription.await(Duration.ofSeconds(2));
//        listenerContainer.start();
//
//    }
//
//    @Override
//    public void destroy() throws Exception {
//        if(this.subscription != null){
//            this.subscription.cancel();
//        }
//        if(this.listenerContainer != null){
//            this.listenerContainer .stop();
//        }
//    }
//
//}
