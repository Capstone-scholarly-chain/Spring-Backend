package Baeksa.money.global.fcm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.ArrayList;
import java.util.List;

@RedisHash("fcmToken")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FcmToken {

    @Id
    private Long studentId;

//    private String fcmToken;
    private List<String> fcmTokens = new ArrayList<>();

    public FcmToken(Long studentId) {
        this.studentId = studentId;
        this.fcmTokens = new ArrayList<>();
        //리스트는 생성자 어노테이션이 초기화해주지 않으므로 꼭 쓰기
    }
}