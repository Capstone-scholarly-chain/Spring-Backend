//package Baeksa.money.domain.streams;
//
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
//import org.springframework.data.redis.connection.stream.Consumer;
//import org.springframework.data.redis.connection.stream.MapRecord;
//import org.springframework.data.redis.connection.stream.ReadOffset;
//import org.springframework.data.redis.connection.stream.StreamOffset;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
//import org.springframework.data.redis.stream.StreamListener;
//import org.springframework.data.redis.stream.StreamMessageListenerContainer;
//import org.springframework.data.redis.stream.Subscription;
//import org.springframework.stereotype.Component;
//
//import java.time.Duration;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class StreamsConsumer implements StreamListener<String, MapRecord<String, Object, Object>>,
//        InitializingBean, DisposableBean {
//
//
//    private final StreamMessageListenerContainer<String,
//            MapRecord<String, Object, Object>> listenerContainer;
//    private Subscription subscription;
////    private String streamKey;
////    private String consumerGroupName;
////    private String consumerName;
//    private final RedisOperator redisOperator;
//    private RedisTemplate<String, Object> redisTemplate;
//
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
