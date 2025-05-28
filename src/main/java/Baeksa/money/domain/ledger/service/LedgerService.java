package Baeksa.money.domain.ledger.service;

import Baeksa.money.domain.committee.service.RequestResponseTracker;
import Baeksa.money.domain.ledger.dto.PendingDepositDto;
import Baeksa.money.domain.ledger.dto.VoteDto;
import Baeksa.money.domain.streams.service.RedisStreamProducer;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RequestResponseTracker requestTracker;
    private final RedisStreamProducer redisStreamProducer;

    private static final int REQUEST_TIMEOUT_SECONDS = 3;


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
                redisTemplate.opsForValue().set("ledger:Theme-balance:" + theme, json);
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
                redisTemplate.opsForValue().set("ledger:Theme-balances", json);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.ALL_THEME_BALANCE_FETCH_FAILED);
            }
            return balances;
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_FAILED);
        }
    }

    /// ///////////////////////////get////////////////////

    public List<Map<String, Object>> getPendingDeposits() {
        try {
            // 🔥 Redis에서 JSON 문자열로 저장된 데이터 조회
            String cachedData = redisTemplate.opsForValue().get("ledger:pending-deposits");
            log.info("이건읽냐");

            if (cachedData != null && !cachedData.isEmpty()) {
                try {
                    // JSON 문자열을 List<Map>으로 파싱
                    List<Map<String, Object>> result = objectMapper.readValue(
                            cachedData,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );
                    log.info("캐시에서 대기중인 입금 요청 조회: {} 건", result.size());
                    return result;
                } catch (Exception e) {
                    log.warn("캐시된 입금 요청 데이터 파싱 실패, 재요청: {}", e.getMessage());
                    redisTemplate.delete("ledger:pending-deposits"); // 잘못된 데이터 삭제
                }
            }

            // 캐시에 없거나 파싱 실패 시 NestJS에 요청
            log.info("캐시에 대기중인 입금 요청 없음, NestJS에 요청");
            return requestPendingDeposits();

        } catch (Exception e) {
            log.error("대기중인 입금 항목 조회 실패", e);
            throw new CustomException(ErrorCode.PENDING_DEPOSIT_FETCH_FAILED);
        }
    }

    private List<Map<String, Object>> requestPendingDeposits() throws InterruptedException, JsonProcessingException {
        String recordId = redisStreamProducer.sendMessage("대기중인 입금 요청", "GET_PENDING_DEPOSITS").toString();
        CountDownLatch latch = requestTracker.registerRequest(recordId);

        try {
            log.info("대기중인 입금 요청 발송: recordId={}", recordId);   //여기까지 로그
            //nest가 보낸 거 받는데, 파싱 실패

            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!receivedInTime) {
                log.warn("대기중인 입금 요청 응답 타임아웃: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            if (!requestTracker.isRequestSuccessful(recordId)) {
                log.warn("대기중인 입금 요청 응답 실패: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_FAILED);
            }

            // 응답 데이터 조회
            String data = redisTemplate.opsForValue().get("ledger:pending-deposits");

            if (data == null) {
                log.warn("대기중인 입금 요청 응답 후에도 데이터 없음: recordId={}", recordId);
                return new ArrayList<>(); // 빈 리스트 반환
            }

            // JSON 문자열을 List<Map>으로 파싱
            return objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {});

        } finally {
            requestTracker.cleanupRequest(recordId);
        }
    }


    public List<Map<String, Object>> getPendingWithdrawals() {
        try {
            String cachedData = redisTemplate.opsForValue().get("ledger:pending-withdraws");

            if (cachedData != null && !cachedData.isEmpty()) {
                try {
                    // JSON 문자열을 List<Map>으로 파싱
                    List<Map<String, Object>> result = objectMapper.readValue(
                            cachedData,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );
                    log.info("캐시에서 대기중인 입금 요청 조회: {} 건", result.size());
                    return result;
                } catch (Exception e) {
                    log.warn("캐시된 입금 요청 데이터 파싱 실패, 재요청: {}", e.getMessage());
                    redisTemplate.delete("ledger:pending-withdraws"); // 잘못된 데이터 삭제
                }
            }

            // 캐시에 없거나 파싱 실패 시 NestJS에 요청
            log.info("캐시에 대기중인 입금 요청 없음, NestJS에 요청");
            return requestPendingWithdrawals();

        } catch (Exception e) {
            log.error("대기중인 입금 항목 조회 실패", e);
            throw new CustomException(ErrorCode.PENDING_DEPOSIT_FETCH_FAILED);//예외 해라
        }
    }


    private List<Map<String, Object>> requestPendingWithdrawals() throws InterruptedException, JsonProcessingException {
        String recordId = redisStreamProducer.sendMessage("대기중인 입금 요청", "GET_PENDING_WITHDRAW").toString();
        CountDownLatch latch = requestTracker.registerRequest(recordId);

        try {
            log.info("대기중인 입금 요청 발송: recordId={}", recordId);   //여기까지 로그
            //nest가 보낸 거 받는데, 파싱 실패

            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!receivedInTime) {
                log.warn("대기중인 입금 요청 응답 타임아웃: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            if (!requestTracker.isRequestSuccessful(recordId)) {
                log.warn("대기중인 입금 요청 응답 실패: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_FAILED);
            }

            // 응답 데이터 조회
            String data = redisTemplate.opsForValue().get("ledger:pending-withdraws");

            if (data == null) {
                log.warn("대기중인 입금 요청 응답 후에도 데이터 없음: recordId={}", recordId);
                return new ArrayList<>(); // 빈 리스트 반환
            }

            // JSON 문자열을 List<Map>으로 파싱
            return objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {});

        } finally {
            requestTracker.cleanupRequest(recordId);
        }
    }





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


    public VoteDto.setVodeDto getVoteStatus(String requestId) {

        String s;
        VoteDto.setVodeDto result = null;
        try {
            s = redisTemplate.opsForValue().get("ledger:withdraw-vote:" + requestId);
            if (s == null) {
                result = requestVoteStatus(requestId);
                log.info("result: {}", result);
            }
            log.info("s2: {}", s);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.VOTE_STATUS_FETCH_FAILED);
        }
        return result;
    }
    private VoteDto.setVodeDto requestVoteStatus(String requestId) throws InterruptedException {
        String recordId = redisStreamProducer.sendMessageLedgerEntryId(requestId, "GET_VOTE_STATUS").toString();
        CountDownLatch latch = requestTracker.registerRequest(recordId);

        try {
            log.debug("사용자 가입 상태 요청 발행: {}, 대상 ID: {}", recordId, requestId);

            // 응답 대기
            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!receivedInTime) {
                log.warn("대기중인 입금 요청 응답 타임아웃: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            if (!requestTracker.isRequestSuccessful(recordId)) {
                log.warn("대기중인 입금 요청 응답 실패: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_FAILED);
            }

            // 응답을 받았으면 Redis에서 다시 조회
            String key = "ledger:withdraw-vote:" + requestId;
            log.info("key: {}", key);
            String value = redisTemplate.opsForValue().get(key);
            VoteDto.setVodeDto result = null;
            try {
                result = objectMapper.readValue(
                        value,
                        new TypeReference<VoteDto.setVodeDto>() {
                        }
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            log.info("result: {}", result);

            if (result == null) {
                log.warn("사용자 가입 상태 응답 후에도 값이 없음: {}", requestId);
                throw new CustomException(ErrorCode.DATA_NOT_AVAILABLE);    //데이터 없어서 터진듯
            }

            log.info("사용자 가입 상태 조회 성공: {}", requestId);
            return result;
//            return objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
        } finally {
            // 완료된 요청 정리
            requestTracker.cleanupRequest(recordId);
        }
    }


    public VoteDto.ThemeBalanceDto getThemeBalance(String theme) {

        String s ;
        VoteDto.ThemeBalanceDto result = null;
        try {
            s = redisTemplate.opsForValue().get("ledger:Theme-balance:" + theme);
            if (s == null) {
                result = requestThemeBalance(theme);
                log.info("result: {}", result);
            }
            log.info("ledger:Theme-balance: {}", s);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ONE_THEME_BALANCE_FETCH_FAILED);
        }
        return result;
    }

    private VoteDto.ThemeBalanceDto requestThemeBalance(String theme) throws InterruptedException {
        String recordId = redisStreamProducer.sendMessageTheme(theme, "GET_THEME_BALANCE").toString();
        CountDownLatch latch = requestTracker.registerRequest(recordId);

        try {
            log.debug("테마 잔액 조회: {}, 대상 ID: {}", theme, recordId);

            // 응답 대기
            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!receivedInTime) {
                log.warn("테마 잔액 조회 응답 타임아웃: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            if (!requestTracker.isRequestSuccessful(recordId)) {
                log.warn("테마 잔액 조회 요청 응답 실패: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_FAILED);
            }

            // 응답을 받았으면 Redis에서 다시 조회
            String key = "ledger:theme-balance:" + theme;
            log.info("key: {}", key);
            String value = redisTemplate.opsForValue().get(key);
            VoteDto.ThemeBalanceDto result = null;
            try {
                result = objectMapper.readValue(
                        value,
                        new TypeReference<VoteDto.ThemeBalanceDto>() {
                        }
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            log.info("result: {}", result);

            if (result == null) {
                log.warn("사용자 가입 상태 응답 후에도 값이 없음: {}", recordId);
                throw new CustomException(ErrorCode.DATA_NOT_AVAILABLE);    //데이터 없어서 터진듯
            }

            log.info("사용자 가입 상태 조회 성공: {}", recordId);
            return result;

        } finally {
            // 완료된 요청 정리
            requestTracker.cleanupRequest(recordId);
        }
    }



    public List<VoteDto.ThemeBalanceDto> getAllThemeBalance() {

        try {
            String cachedData = redisTemplate.opsForValue().get("ledger:theme-balances");
            log.info("이건읽냐");

            if (cachedData != null && !cachedData.isEmpty()) {
                try {
                    // JSON 문자열을 List<Map>으로 파싱
                    List<VoteDto.ThemeBalanceDto> result = objectMapper.readValue(
                            cachedData,
                            new TypeReference<List<VoteDto.ThemeBalanceDto>>() {}
                    );
                    log.info("캐시에서 대기중인 모든 테마 조회: {} 건", result.size());
                    return result;
                } catch (Exception e) {
                    log.warn("캐시된 모든 테마 데이터 파싱 실패, 재요청: {}", e.getMessage());
                    redisTemplate.delete("ledger:theme-balances"); // 잘못된 데이터 삭제
                }
            }
            log.info("캐시에 대기중인 모든 테마 없음, NestJS에 요청");
            return requestAllThemeBalance();

        } catch (Exception e) {
            log.error("대기중인 모든 테마 조회 실패", e);
            throw new CustomException(ErrorCode.PENDING_DEPOSIT_FETCH_FAILED);
        }
    }

    private List<VoteDto.ThemeBalanceDto> requestAllThemeBalance() throws InterruptedException, JsonProcessingException {
        String recordId = redisStreamProducer.sendMessage("모든 테마 잔액 조회", "GET_ALL_THEME_BALANCE").toString();
        CountDownLatch latch = requestTracker.registerRequest(recordId);

        try {
            log.info("대기중인 모든 테마 발송: recordId={}", recordId);

            boolean receivedInTime = latch.await(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!receivedInTime) {
                log.warn("대기중인 모든 테마 응답 타임아웃: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_TIMEOUT);
            }

            if (!requestTracker.isRequestSuccessful(recordId)) {
                log.warn("대기중인 모든 테마 응답 실패: recordId={}", recordId);
                throw new CustomException(ErrorCode.REQUEST_FAILED);
            }

            // 응답 데이터 조회
            String data = redisTemplate.opsForValue().get("ledger:theme-balances");

            if (data == null) {
                log.warn("대기중인 모든 테마 응답 후에도 데이터 없음: recordId={}", recordId);
                return new ArrayList<>(); // 빈 리스트 반환
            }

            // JSON 문자열을 List<Map>으로 파싱
            return objectMapper.readValue(data, new TypeReference<List<VoteDto.ThemeBalanceDto>>() {});

        } finally {
            requestTracker.cleanupRequest(recordId);
        }
    }
}