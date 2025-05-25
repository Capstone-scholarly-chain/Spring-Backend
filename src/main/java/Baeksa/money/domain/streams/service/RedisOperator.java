package Baeksa.money.domain.streams.service;





import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisOperator {

//    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public Object getRedisValue(String key, String field){
        return this.stringRedisTemplate.opsForHash().get(key, field);
    }

    public long increaseRedisValue(String key, String field){
        return this.stringRedisTemplate.opsForHash().increment(key, field, 1);
    }

    public void ackStream(String consumerGroupName, MapRecord<String, String, String> message){
        this.stringRedisTemplate.opsForStream().acknowledge(consumerGroupName, message);
    }

    public void claimStream(PendingMessage pendingMessage, String consumerName){
        RedisAsyncCommands commands = (RedisAsyncCommands) this.stringRedisTemplate
                .getConnectionFactory().getConnection().getNativeConnection();

        CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                .add(pendingMessage.getIdAsString())
                .add(pendingMessage.getGroupName())
                .add(consumerName)
                .add("20")
                .add(pendingMessage.getIdAsString());
        commands.dispatch(CommandType.XCLAIM, new StatusOutput(StringCodec.UTF8), args);
    }

    //start와 end에 동일한 id를 넣으니까 해당 id의 하나의 값(고유)만 출력
    public MapRecord<String, String, String> findStreamMessageById(String streamKey, String id){
        List<MapRecord<String, String, String>> mapRecordList = this.findStreamMessageByRange(streamKey, id, id);
        if(mapRecordList.isEmpty()) return null;
        return mapRecordList.get(0);
    }

    @SuppressWarnings("unchecked")
    public List<MapRecord<String, String, String>> findStreamMessageByRange(String streamKey, String startId, String endId) {
        return (List<MapRecord<String, String, String>>) (List<?>) stringRedisTemplate
                .opsForStream()
                .range(streamKey, Range.closed(startId, endId));
    }

//    public List<MapRecord<String, String, String>> findStreamMessageByRange(String streamKey, String startId, String endId){
//        return this.stringRedisTemplate.opsForStream().range(streamKey, Range.closed(startId, endId));
//    }


    public void createStreamConsumerGroup(String streamKey, String consumerGroupName){
        // if stream is not exist, create stream and consumer group of it
        if (Boolean.FALSE.equals(this.stringRedisTemplate.hasKey(streamKey))){
            RedisAsyncCommands commands = (RedisAsyncCommands) this.stringRedisTemplate
                    .getConnectionFactory()
                    .getConnection()
                    .getNativeConnection();

            CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                    .add(CommandKeyword.CREATE)
                    .add(streamKey)
                    .add(consumerGroupName)
                    .add("0")
                    .add("MKSTREAM");

            commands.dispatch(CommandType.XGROUP, new StatusOutput(StringCodec.UTF8), args);
        }
        // stream is exist, create consumerGroup if is not exist
        else{
            if(!isStreamConsumerGroupExist(streamKey, consumerGroupName)){
                this.stringRedisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroupName);
            }
        }
    }

    public PendingMessages findStreamPendingMessages(String streamKey, String consumerGroupName, String consumerName){
        return this.stringRedisTemplate.opsForStream()
                .pending(streamKey, Consumer.from(consumerGroupName, consumerName), Range.unbounded(), 100L);
    }

    public boolean isStreamConsumerGroupExist(String streamKey, String consumerGroupName){
        Iterator<StreamInfo.XInfoGroup> iterator = this.stringRedisTemplate
                .opsForStream().groups(streamKey).stream().iterator();

        while(iterator.hasNext()){
            StreamInfo.XInfoGroup xInfoGroup = iterator.next();
            if(xInfoGroup.groupName().equals(consumerGroupName)){
                return true;
            }
        }
        return false;
    }

    // 메시지 리스너 컨테이너 역할을 함. stream에서 레코드를 사용하고, StreamListener 해당 스트림에 주입된 인스턴스를 구동하는 데 사용
    // 메시지 수신 및 처리를 담당.
    public StreamMessageListenerContainer createStreamMessageListenerContainer(){
        return StreamMessageListenerContainer.create(
                this.stringRedisTemplate.getConnectionFactory(),
                StreamMessageListenerContainer
                        .StreamMessageListenerContainerOptions.builder()
                        .hashKeySerializer(new StringRedisSerializer())
                        .hashValueSerializer(new StringRedisSerializer())
                        .pollTimeout(Duration.ofMillis(20))
                        .build()
        );
    }
}
