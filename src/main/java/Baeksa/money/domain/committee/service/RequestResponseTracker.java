package Baeksa.money.domain.committee.service;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RequestResponseTracker {
    // requestId를 키로 하는 대기 중인 요청 맵
    private final Map<String, CountDownLatch> pendingRequests = new ConcurrentHashMap<>();
    // requestId를 키로 하는 요청 성공 여부 맵
    private final Map<String, AtomicBoolean> requestResults = new ConcurrentHashMap<>();

    /**
     * 새 요청을 등록합니다.
     * @param requestId 요청 식별자
     * @return 응답 대기를 위한 CountDownLatch
     */
    public CountDownLatch registerRequest(String requestId) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean result = new AtomicBoolean(false);

        pendingRequests.put(requestId, latch);
        requestResults.put(requestId, result);

        return latch;
    }

    /**
     * 요청에 대한 응답이 도착했음을 처리합니다.
     * @param requestId 요청 식별자
     */
    public void markRequestCompleted(String requestId) {
        AtomicBoolean result = requestResults.get(requestId);
        if (result != null) {
            result.set(true);
        }

        CountDownLatch latch = pendingRequests.get(requestId);
        if (latch != null) {
            latch.countDown();
        }
    }

    /**
     * 요청의 성공 여부를 확인합니다.
     * @param requestId 요청 식별자
     * @return 요청 성공 여부
     */
    public boolean isRequestSuccessful(String requestId) {
        AtomicBoolean result = requestResults.get(requestId);
        return result != null && result.get();
    }

    /**
     * 완료된 요청 정보를 정리합니다.
     * @param requestId 요청 식별자
     */
    public void cleanupRequest(String requestId) {
        pendingRequests.remove(requestId);
        requestResults.remove(requestId);
    }
}