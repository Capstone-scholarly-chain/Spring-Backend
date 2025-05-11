package Baeksa.money.global.redis.service;

import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.enums.Status;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    //        MemberEntity member = memberService.findById(userDetails.getStudentId());
    //        redisService.ValidStatus(member, member.getStatus());
    public void ValidStatus(MemberEntity entity, Status status){
        if(entity.getStatus() != Status.APPROVE){
            throw new CustomException(ErrorCode.NOTSET_STATUS);
        }
    }

    public static String[] getParts(String id) {
        String[] parts = id.split("_");
        if (parts.length < 3) {
            log.warn("ID 형식이 잘못되었습니다: {}", id);
            if (parts == null) {
                log.error("수신된 메시지에 studentId 필드가 없습니다.");
                return null;
            }
        }
        return parts;
    }


//    public String unwrapRedisString(Object redisValue) {
//        if (redisValue instanceof String str) {
//            if (str.startsWith("\"") && str.endsWith("\"")) {
//                return str.substring(1, str.length() - 1);
//            }
//            return str;
//        }
//        return null;
//    }

}
