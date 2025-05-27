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
    public CountDownLatch registerRequest(String recordId) {
        CountDownLatch latch = new CountDownLatch(1);
        log.info("요청 등록: {}", recordId);
        //count가 1보다 작으면 illegal
        pendingRequests.put(recordId, latch);
        requestResults.put(recordId, new AtomicBoolean(false));

        log.info("요청 등록: {}", recordId);
        return latch;
    }

    /**
     * 요청 완료 처리
     */
    public void markRequestCompleted(String recordId) {
        AtomicBoolean result = requestResults.get(recordId);
        if (result != null) {
            result.set(true);
        }

        CountDownLatch latch = pendingRequests.get(recordId);
        if (latch != null) {
            latch.countDown();
            log.debug("요청 완료 처리: {}", recordId);
        } else {
            log.warn("알 수 없는 요청 ID: {}", recordId);
        }
    }

    /**
     * 요청 성공 확인
     */
    public boolean isRequestSuccessful(String recordId) {
        AtomicBoolean result = null;
        try {
            result = requestResults.get(recordId);
            log.info("[result recordId ]:{}", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        log.info("[ track안에 recordId ]: {}" , recordId);
        return result != null && result.get();
    }

    /**
     * 완료된 요청 정리
     */
    public void cleanupRequest(String recordId) {
        pendingRequests.remove(recordId);
        requestResults.remove(recordId);
        log.debug("요청 정리: {}", recordId);
    }
}