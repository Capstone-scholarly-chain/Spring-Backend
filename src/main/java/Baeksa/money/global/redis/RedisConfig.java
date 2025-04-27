package Baeksa.money.global.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import javax.sound.midi.Receiver;

@Configuration
@EnableRedisRepositories(basePackages = "Baeksa.money.global.redis")
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        LettuceConnectionFactory factory = new LettuceConnectionFactory(host, port);
        factory.setPassword(password); // 비밀번호 설정
        return factory;
    }

//    /// 추가
//    // container 빈으로 등록
//    @Bean
//    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
//                                            MessageListenerAdapter listenerAdapter) {
//
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        // connection factory
//        container.setConnectionFactory(connectionFactory);
//        // listenerAdapter 를 이용해서 Listener 지정
//        // subscribe 할 topic 지정
//        container.addMessageListener(listenerAdapter, new PatternTopic("ChannelTopic 이름"));
//
//        return container;
//    }
//
//    // MessageListenerAdapter 에서 receiver 설정
//    @Bean
//    MessageListenerAdapter listenerAdapter(Receiver receiver) {
//        return new MessageListenerAdapter(receiver, "receiveMessage");
//    }
//
//    // 처리하는 로직
//    @Bean
//    Receiver receiver() {
//        return new Receiver();
//    }
//
//    // redis template
//    @Bean
//    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
//        return new StringRedisTemplate(connectionFactory);
//    }

}
