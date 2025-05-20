package Baeksa.money.domain.streams.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisOperator {

    private final RedisTemplate<String, Object> redisTemplate;


    public RecordId addMessage(String streamKey, Map<String, String> data) {
        StringRecord record = StreamRecords.string(data).withStreamKey(streamKey);
        return redisTemplate.opsForStream().add(record);
    }

    public List<MapRecord<String, Object, Object>> readMessages(String streamKey, String group, String consumer) {
        return redisTemplate.opsForStream().read(
                Consumer.from(group, consumer),
                StreamReadOptions.empty().count(10).block(Duration.ofSeconds(2)),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed())
        );
    }

    public void acknowledgeMessage(String streamKey, String group, RecordId recordId) {
        redisTemplate.opsForStream().acknowledge(streamKey, group, recordId);
    }
}
