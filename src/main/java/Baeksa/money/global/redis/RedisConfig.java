package Baeksa.money.global.redis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
@EnableRedisRepositories(basePackages = {
        "Baeksa.money.global.redis",
//        "Baeksa.money.global.fcm"
})
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;


//    @Bean
//    public RedisConnectionFactory redisConnectionFactory(){
//        LettuceConnectionFactory factory = new LettuceConnectionFactory(host, port);
//        return factory;
//    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        LettuceConnectionFactory factory = new LettuceConnectionFactory(host, port);
        factory.setPassword(password); // 비밀번호 설정
        return factory;
    }

    // RedisTemplate 설정
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper mapper) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Long> redisTemplateLong(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Long.class));

        return template;
    }

    @Bean
    public RedisTemplate<String, Integer> redisTemplateInteger(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer()); // 정수를 문자열로 자동 변환
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

//    @Bean
//    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory factory,
//                                                              RedisSubscriber subscriber,
//                                                              CommitteeSubscriber committeeSubscriber,
//                                                              LedgerSubscriber ledgerSubService){
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(factory);
//
//        // 채널 지정 - 22개
////        container.addMessageListener(committeeSubscriber, new ChannelTopic("test.publisher.request"));
////        container.addMessageListener(committeeSubscriber, new ChannelTopic("nestjs:response:register-user"));
////        container.addMessageListener(committeeSubscriber, new ChannelTopic("nestjs:response:membership:updated"));
////        container.addMessageListener(committeeSubscriber, new ChannelTopic("nestjs:response:membership:approve"));
////        container.addMessageListener(committeeSubscriber, new ChannelTopic("nestjs:response:membership:rejected"));
////        container.addMessageListener(ledgerSubService, new ChannelTopic("nestjs:response:deposit:created"));
////        container.addMessageListener(ledgerSubService, new ChannelTopic("nestjs:response:deposit:updated"));
////        container.addMessageListener(ledgerSubService, new ChannelTopic("nestjs:response:deposit:approved"));
////        container.addMessageListener(ledgerSubService, new ChannelTopic("nestjs:response:deposit:rejected"));
////        container.addMessageListener(ledgerSubService, new ChannelTopic("nestjs:response:withdraw:created"));
////        container.addMessageListener(ledgerSubService, new ChannelTopic("nestjs:response:withdraw:updated"));
////        container.addMessageListener(subscriber, new ChannelTopic("nestjs:response:withdraw:result"));
////        container.addMessageListener(subscriber, new ChannelTopic("nestjs:response:error"));
////        container.addMessageListener(subscriber, new ChannelTopic("nestjs:response:student-count"));
////        container.addMessageListener(subscriber, new ChannelTopic("nestjs:response:student-council-count"));
////        container.addMessageListener(subscriber, new ChannelTopic("nestjs:response:pending-register"));
////        container.addMessageListener(subscriber, new ChannelTopic("nestjs:response:status-register"));
////        container.addMessageListener(subscriber, new ChannelTopic("nestjs:response:pending-deposits"));
////        container.addMessageListener(subscriber, new ChannelTopic("nestjs:response:pending-withdraws"));
////        container.addMessageListener(subscriber, new ChannelTopic("nestjs:response:withdraw-vote-status"));
////        container.addMessageListener(subscriber, new ChannelTopic("nestjs:response:thema-balance"));
////        container.addMessageListener(subscriber, new ChannelTopic("nestjs:response:thema-balances"));
//        return container;
//    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // yyyy-MM-dd'T'HH:mm:ss 형태로 출력

        return mapper;
    }

}
