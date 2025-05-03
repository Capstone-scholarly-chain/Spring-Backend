package Baeksa.money.global.redis;


import Baeksa.money.domain.Dto.MemberDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }

//    public void publish(String channel, MemberDto.MemberResponseDto dto) {
//        redisTemplate.convertAndSend(channel, dto);
//    }
}
