package Baeksa.money.global.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.connection.MessageListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {
            String publishMessage = redisTemplate.getStringSerializer().deserialize(message.getBody());

            RedisDto.MessageDto redisDto = objectMapper.readValue(publishMessage, RedisDto.MessageDto.class);

            log.info("Redis Subscribe Channel : " + redisDto.getChannel());
            log.info("Redis SUB Message : {}", publishMessage);
        }
        catch (JsonProcessingException e){
            log.error(e.getMessage());
        }
    }
}
