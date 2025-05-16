package Baeksa.money.global.redis.service;

import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.enums.Status;
import Baeksa.money.domain.committee.service.RequestResponseTracker;
import Baeksa.money.domain.student.dto.StudentDto;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RequestResponseTracker requestTracker;

    private static final int REQUEST_TIMEOUT_SECONDS = 3;

//    //requestId를 받고
//    private String waitResponseFromRequestId() throws InterruptedException {
//
//        String requestId = UUID.randomUUID().toString();
//        // 요청 등록
//        CountDownLatch latch = requestTracker.registerRequest(requestId);
//
//        try {
//            // Redis에 직접 메시지 발행
//            // JSON 형식으로 메시지 구성
//            Map<String, String> messageMap = new HashMap<>();
//            messageMap.put("requestId", requestId);
//
//            String message = null;
//            try {
//                message = objectMapper.writeValueAsString(messageMap);
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
//
////            String message = requestId;
////            redisTemplate.convertAndSend(PENDING_REGISTER_CHANNEL, message);
////            log.debug("대기 중인 가입 요청 목록 요청 발행: {}", requestId);
//
//            // 응답 대기
//            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
//
//            // 응답 확인
//            if (!receivedInTime) {
//                log.warn("time: {}", receivedInTime);
//                if (!requestTracker.isRequestSuccessful(requestId))
//                    log.warn("대기 중인 가입 요청 목록 응답 타임아웃: {}", requestId);
//                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
//            }
//
//            /////return null;
//        } finally {
//            // 완료된 요청 정리
//            requestTracker.cleanupRequest(requestId);
//        }
//    }

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
