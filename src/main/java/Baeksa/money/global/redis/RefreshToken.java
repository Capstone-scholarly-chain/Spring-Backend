package Baeksa.money.global.redis;

import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "token", timeToLive = 86400)
public class RefreshToken {

    @Id
    private Long studentId;

    private String refreshToken;
}
