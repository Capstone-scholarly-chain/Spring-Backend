package Baeksa.money.domain.ledger.service;

import Baeksa.money.domain.ledger.dto.PendingDepositDto;
import Baeksa.money.domain.ledger.dto.VoteDto;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /// ///값 캐싱 셋 겟 한거

    public void cachePendingDeposits(List<PendingDepositDto> deposits) {
        try {
            String json = objectMapper.writeValueAsString(deposits);
            try {
                redisTemplate.opsForValue().set("ledger:pending-deposits", json);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.DEPOSIT_CACHE_COMPLETE);
            }
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
    }

    public void cachePendingWithdraws(List<PendingDepositDto> deposits) {
        try {
            String json = objectMapper.writeValueAsString(deposits);
            try {
                redisTemplate.opsForValue().set("ledger:pending-withdraws", json);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.WITHDRAW_CACHE_COMPLETE);
            }
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
    }

    public VoteDto.setVodeDto setVoteStatus(VoteDto.setVodeDto voteStatus, String key) {
        try {
            String json = objectMapper.writeValueAsString(voteStatus);
            //단일일때는 문자열로 json을 안하나봄
            // DTO를 JSON 문자열로 직렬화해서 Redis에 저장
            try {
                redisTemplate.opsForValue().set("ledger:withdraw-vote:" + key, json);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.VOTE_REGISTER_FAILED);
            }

            return voteStatus;
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
    }

    //theme를 계속 변경할거임
    public VoteDto.ThemeBalanceDto setThemeBalance(VoteDto.ThemeBalanceDto themeBalanceDto, String theme) {
        try {
            String json = objectMapper.writeValueAsString(themeBalanceDto);
            //단일일때는 문자열로 json을 안하나봄
            // DTO를 JSON 문자열로 직렬화해서 Redis에 저장
            try {
                redisTemplate.opsForValue().set("ledger:theme-balance:" + theme, json);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.ONE_THEME_BALANCE_REGISTER_FAILED);
            }

            return themeBalanceDto;
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
    }

    public List<VoteDto.ThemeBalanceDto> setAllThemeBalance(List<VoteDto.ThemeBalanceDto> balances) {
        try {
            String json = objectMapper.writeValueAsString(balances);
            try {
                redisTemplate.opsForValue().set("ledger:theme-balances", json);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.ALL_THEME_BALANCE_FETCH_FAILED);
            }
            return balances;
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
    }


//    public List<String> getLedgerValues() {
//        Set<String> keys = redisTemplate.keys("LEDGER_*");
//        List<String> result = new ArrayList<>();
//        if (keys != null) {
//            for (String key : keys) {
//                String value = redisTemplate.opsForValue().get(key);
//                System.out.println("Key: " + key + ", Value: " + value);
//                result.add(value);
//            }
//        }
//        return result;
//    }

    public List<String> getPendingDeposits() {
        try {
            Set<String> keys = redisTemplate.keys("ledger:pending-deposits:*");
            List<String> result = new ArrayList<>();
            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                System.out.println("Key: " + key + ", Value: " + value);
                result.add(value);
            }
            return result;
        } catch (Exception e) {
            log.error("대기중인 입금 항목 조회 실패", e);
            throw new CustomException(ErrorCode.PENDING_DEPOSIT_FETCH_FAILED);
        }
    }

    public List<String> getPendingWithdrawals() {
        try {
            Set<String> keys = redisTemplate.keys("ledger:pending-withdraws:*");
            List<String> result = new ArrayList<>();

            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                System.out.println("Key: " + key + ", Value: " + value);
                result.add(value);
            }
            return result;
        } catch (Exception e) {
            log.error("대기중인 입금 항목 조회 실패", e);
            throw new CustomException(ErrorCode.PENDING_WITHDRAW_FETCH_FAILED);
        }
    }


    public List<VoteDto.ThemeBalanceDto> getAllThemeBalance() {

        try {
            String s;
            try {
                s = redisTemplate.opsForValue().get("ledger:theme-balances");
            } catch (Exception e) {
                throw new CustomException(ErrorCode.TOTAL_BALANCE_FETCH_FAILED);
            }
            List<VoteDto.ThemeBalanceDto> result = objectMapper.readValue(
                    s, new TypeReference<List<VoteDto.ThemeBalanceDto>>() {
                    }
            );

            return result;
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
    }

//    public List<String> getMyList(Long studentId) {
//        Set<String> keys = redisTemplate.keys("*" + studentId + "*");
//        List<String> result = new ArrayList<>();
//        for (String key : keys) {
//            String value = redisTemplate.opsForValue().get(key);
//            System.out.println("Key: " + key + ", Value: " + value);
//            result.add(value);
//        }
//        return result;
//    }


    public List<Map<String, Object>> getMyList2(String studentId) {
        Set<String> keys = redisTemplate.keys("*" + studentId + "*");
        List<Map<String, Object>> result = new ArrayList<>();
        for (String key : keys) {
            String value;
            try {
                value = redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.MY_HISTORY_FETCH_FAILED);
            }
            try {
                Map<String, Object> map = objectMapper.readValue(
                        value,
                        new TypeReference<Map<String, Object>>() {}
                );
                result.add(map);
            } catch (JsonProcessingException e) {
                log.warn("JSON 파싱 실패. key: {}, value: {}", key, value);
                throw new CustomException(ErrorCode.JSON_FAILED);
            }
        }
        return result;
    }

    public List<PendingDepositDto> getPendingDummyDeposits() {

        try {
            String s;
            try {
                s = redisTemplate.opsForValue().get("ledger:pending-deposits");
            } catch (Exception e) {
                throw new CustomException(ErrorCode.DEPOSIT_CACHE_FAILED);
            }
            List<PendingDepositDto> result = objectMapper.readValue(
                    s,
                    new TypeReference<List<PendingDepositDto>>() {
                    }
            );
            return result;
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
    }


    public List<PendingDepositDto> getPendingDummyWithdraws() {
        try {
            String s = null;
            try {
                s = redisTemplate.opsForValue().get("ledger:pending-withdraws");
            } catch (Exception e) {
                throw new CustomException(ErrorCode.WITHDRAW_CACHE_FAILED);
            }
            List<PendingDepositDto> result = objectMapper.readValue(
                    s,
                    new TypeReference<List<PendingDepositDto>>() {
                    }
            );
            return result;
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
    }




    public VoteDto.setVodeDto getVoteStatus(String key) {
        try {
            String s;
            try {
                s = redisTemplate.opsForValue().get("ledger:withdraw-vote:" + key);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.VOTE_STATUS_FETCH_FAILED);
            }
            VoteDto.setVodeDto result = objectMapper.readValue(
                    s,
                    new TypeReference<VoteDto.setVodeDto>() {
                    }
            );

            return result;
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
    }


    public VoteDto.ThemeBalanceDto getThemeBalance(String theme) {
        try {
            String s ;
            try {
                s = redisTemplate.opsForValue().get("ledger:theme-balance:" + theme);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.ONE_THEME_BALANCE_FETCH_FAILED);
            }
            VoteDto.ThemeBalanceDto result = objectMapper.readValue(
                    s,
                    new TypeReference<VoteDto.ThemeBalanceDto>() {
                    }
            );
            return result;
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
    }
    //    private String requestPendingRegisters() throws InterruptedException {
    //        // 요청 식별자 생성
    //        String requestId = UUID.randomUUID().toString();
    //
    //        // 요청 등록
    //        CountDownLatch latch = requestTracker.registerRequest(requestId);
    //
    //        try {
    //            // Redis에 직접 메시지 발행
    //            // JSON 형식으로 메시지 구성
    //            Map<String, String> messageMap = new HashMap<>();
    //            messageMap.put("requestId", requestId);
    //            messageMap.put("type", "pending-register-request");
    //            String message = null;
    //            try {
    //                message = objectMapper.writeValueAsString(messageMap);
    //            } catch (JsonProcessingException e) {
    //                e.printStackTrace();
    //                throw new RuntimeException(e);
    //            }
    //
    //            // Redis에 JSON 메시지 발행
    //            redisTemplate.convertAndSend(PENDING_REGISTER_CHANNEL, message);
    //            log.debug("대기 중인 가입 요청 목록 요청 발행: {}", requestId);
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
    //                if(!requestTracker.isRequestSuccessful(requestId))
    //                log.warn("대기 중인 가입 요청 목록 응답 타임아웃: {}", requestId);
    //                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
    //            }
    //
    //            // 응답을 받았으면 Redis에서 다시 조회
    //            String value = null;
    //            try {
    //                value = redisTemplate.opsForValue().get(PENDING_REGISTER_KEY);
    //                log.info("value: {}", value);
    //            } catch (Exception e) {
    //                throw new RuntimeException(e);
    //            }
    //            if (value == null) {
    //                log.warn("대기 중인 가입 요청 목록 응답 후에도 값이 없음: {}", requestId);
    //                throw new CustomException(ErrorCode.DATA_NOT_AVAILABLE);    //우리 이제 값을 받음
    //            }
    //
    //            log.debug("대기 중인 가입 요청 목록 조회 성공");
    //            return value;
    //        } finally {
    //            // 완료된 요청 정리
    //            requestTracker.cleanupRequest(requestId);
    //        }
    //    }

}