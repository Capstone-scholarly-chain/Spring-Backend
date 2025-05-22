//package Baeksa.money.domain.streams.service;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.stream.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.*;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class StreamClaudeService {
//
//    private final StringRedisTemplate stringRedisTemplate;
//    private final ObjectMapper objectMapper;
//
//    // 요청 스트림과 응답 스트림
//    private final String REQUEST_STREAM = "spring-to-nest-stream";
//    private final String RESPONSE_STREAM = "nest-to-spring-stream";
//
//    private static final String GROUP_NAME = "spring-consumer-group";
//
//
//    // 응답 대기를 위한 맵
//    private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();
//    private String lastReadResponseId = "0-0";
//
//    @PostConstruct
//    public void init() {
//        // 응답 스트림을 주기적으로 확인하는 스케줄러 시작
//        startResponseListener();
//    }
//
//    public <T> CompletableFuture<T> processRequest(Object requestData, TypeReference<T> responseType) {
//        String requestId = UUID.randomUUID().toString();
//
//        try {
//            log.info("🚀 Processing client request with ID: {}", requestId);
//
//            // 요청 데이터 준비
//            // recordId는 스트림 내부id이고 nest.js는 알 수 없음.
//            // requestId의 경우 응답 매칭을 위한 추적 ID임. nest.js가 알 수 있음
//            Map<String, String> data = new HashMap<>();
//            data.put("requestId", requestId);
//            data.put("DtoType", requestData.getClass().getName());
//            data.put("payload", objectMapper.writeValueAsString(requestData));
//            data.put("timestamp", String.valueOf(System.currentTimeMillis()));
//            data.put("status", Status.SUCCESS.name()); //테스트용, 나중엔 보낼때 뺴야됨
//            log.info("data: {}", data);
//
//            // 응답을 기다리기 위한 Future 생성
//            CompletableFuture<String> responseFuture = new CompletableFuture<>();
//            pendingRequests.put(requestId, responseFuture);
//
//            // Redis Stream에 요청 메시지 발행
//            StringRecord record = StreamRecords.string(data).withStreamKey(REQUEST_STREAM);
//            RecordId recordId = stringRedisTemplate.opsForStream().add(record);
//            log.info("✅ Request sent to Nest.js requestId: {}, recordId: {}", requestId, recordId);
//
//            // 타임아웃 설정 (예: 30초)
//            CompletableFuture.delayedExecutor(30, TimeUnit.SECONDS).execute(() -> {
//                CompletableFuture future = pendingRequests.remove(requestId);
//                if (future != null && !future.isDone()) {
//                    future.completeExceptionally(new TimeoutException("Response timeout after 30 seconds"));
//                    log.warn("⏱️ Request timed out: {}", requestId);
//                }
//            });
//
//            // 응답이 도착하면 객체로 변환하여 반환
//            return responseFuture.thenApply(jsonResponse -> {
//                try {
//                    return objectMapper.readValue(jsonResponse, responseType);
//                } catch (JsonProcessingException e) {
//                    throw new CompletionException(new RuntimeException("Failed to parse response", e));
//                }
//            });
//
//        } catch (Exception e) {
//            pendingRequests.remove(requestId);
//            CompletableFuture<T> failedFuture = new CompletableFuture<>();
//            failedFuture.completeExceptionally(new RuntimeException("Failed to process request", e));
//            return failedFuture;
//        }
//    }
//
//
//    /**
//     * 응답 리스너 시작
//     */
//    private void startResponseListener() {
//        Thread listenerThread = new Thread(() -> {
//            log.info("🔄 Starting response listener for stream: {}", REQUEST_STREAM);    //이 부분 바꾸기!!!!!!!!!!!!!!!
//
//            while (!Thread.currentThread().isInterrupted()) {
//                try {
//                    // 응답 스트림에서 새 메시지 읽기
//                    List<MapRecord<String, Object, Object>> responses = stringRedisTemplate.opsForStream()
//                            .read(StreamReadOptions.empty().count(10).block(Duration.ofSeconds(2)),
//                                    StreamOffset.create(REQUEST_STREAM, ReadOffset.from(lastReadResponseId)));
//                    //지금 마지막으로 읽은 것을 기록해두고
//                    // 마지막으로 부터 모든걸 읽고 있음. 최신만 읽고싶다? -> ReadOffset.latest()
//
//                    if (responses != null && !responses.isEmpty()) {
//                        for (MapRecord<String, Object, Object> response : responses) {
//                            Map<Object, Object> value = response.getValue();
//                            RecordId recordId = response.getId();
//
//                            // 응답 데이터 추출
//                            String requestId = (String) value.get("requestId");
//                            String payload = (String) value.get("payload");
//                            String status = (String) value.get("status"); // ✅ 반드시 포함 필요
//
//                            log.info("✅ requestId from Nest.js: {}", requestId);
//                            log.info("✅ payload from Nest.js: {}", payload);
//                            log.info("✅ status from Nest.js: {}", status);
//
////                            // 대기 중인 Future 완료
////                            handleResponse(requestId, payload);
//                            if ("SUCCESS".equalsIgnoreCase(status)) {
//                                handleAck(requestId); // 상태 저장/로그용
////                                handleResponse(requestId, payload); // future.complete
//                            } else if ("FAIL".equalsIgnoreCase(status)) {
//                                handleFailure(requestId, payload);
//                            } else {
//                                log.warn("⚠️ Unknown or missing status for requestId={}: {}", requestId, status);
//                            }
//
//                            // 메시지 처리 완료 후 ACK
//                            /////////////////////////////////Stream앞에 바꿀것
//                            stringRedisTemplate.opsForStream().acknowledge(REQUEST_STREAM, GROUP_NAME, response.getId());
//
//
//                            // 마지막으로 읽은 ID 업데이트
//                            lastReadResponseId = response.getId().getValue();
//                        }
//                    }
//
//                } catch (Exception e) {
//                    log.error("❌ Error checking responses", e);
//
//                    // 잠시 대기 후 재시도
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ie) {
//                        Thread.currentThread().interrupt();
//                        break;
//                    }
//                }
//            }
//
//            log.info("Response listener stopped");
//        });
//
//        listenerThread.setDaemon(true);
//        listenerThread.setName("redis-response-listener");
//        listenerThread.start();
//    }
//
//
////    // 응답 완료 (future 완료)
////    public void handleResponse(String requestId, String payload) {
////        CompletableFuture<String> future = pendingRequests.remove(requestId);
////        if (future != null) {
////            future.complete(payload);
////            log.info("✅ Completed response for requestId: {}", requestId);
////        } else {
////            log.warn("⚠️ No matching future found for requestId: {}", requestId);
////        }
////    }
//
//    public void handleAck(String requestId) {
//        log.info("✅ ACK received for requestId: {}", requestId);
//        // 요청 기록 상태 변경, 전송 성공 처리 등
//    }
//
//    public void handleFailure(String requestId, String errorInfo) {
//        log.error("❌ 처리 실패: requestId={}, error={}", requestId, errorInfo);
//        CompletableFuture<String> future = pendingRequests.remove(requestId);
//        if (future != null) {
//            future.completeExceptionally(new RuntimeException("Nest 처리 실패: " + errorInfo));
//        }
//    }
//}