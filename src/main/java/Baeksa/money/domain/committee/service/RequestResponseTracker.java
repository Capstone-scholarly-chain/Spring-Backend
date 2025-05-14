package Baeksa.money.domain.committee.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RequestResponseTracker {

    private final Map<String, CountDownLatch> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> requestResults = new ConcurrentHashMap<>();

    /**
     * 요청 등록
     */
    public CountDownLatch registerRequest(String requestId) {
        CountDownLatch latch = new CountDownLatch(1);
        log.info("요청 등록: {}", requestId);
        //count가 1보다 작으면 illegal
        pendingRequests.put(requestId, latch);
        requestResults.put(requestId, new AtomicBoolean(false));

        log.info("요청 등록: {}", requestId);
        return latch;
    }

    /**
     * 요청 완료 처리
     */
    public void markRequestCompleted(String requestId) {
        AtomicBoolean result = requestResults.get(requestId);
        if (result != null) {
            result.set(true);
        }

        CountDownLatch latch = pendingRequests.get(requestId);
        if (latch != null) {
            latch.countDown();
            log.debug("요청 완료 처리: {}", requestId);
        } else {
            log.warn("알 수 없는 요청 ID: {}", requestId);
        }
    }

    /**
     * 요청 성공 확인
     */
    public boolean isRequestSuccessful(String requestId) {
        AtomicBoolean result = null;
        try {
            result = requestResults.get(requestId);
            log.info("[result]:{}", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        log.info("[ track안에 requestId ]: {}" , requestId);
        return result != null && result.get();
    }

    /**
     * 완료된 요청 정리
     */
    public void cleanupRequest(String requestId) {
        pendingRequests.remove(requestId);
        requestResults.remove(requestId);
        log.debug("요청 정리: {}", requestId);
    }
}