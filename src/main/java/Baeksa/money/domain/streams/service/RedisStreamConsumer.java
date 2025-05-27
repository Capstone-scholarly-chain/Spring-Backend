package Baeksa.money.domain.streams.service;


import Baeksa.money.domain.committee.service.RequestResponseTracker;
import Baeksa.money.domain.fcm.service.FcmService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamConsumer implements StreamListener<String, MapRecord<String, String, String>>,
        InitializingBean, DisposableBean {
    private final RedisTemplate<String, Object> redisTemplate;
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private Subscription subscription;

    private final RedisOperator redisOperator;
    private final ObjectMapper objectMapper;
    private final FcmService fcmService;

    private final RequestResponseTracker requestTracker;
    private final RedisTemplate<String, Integer> redisTemplateInteger;

    private static final String SPRING_TO_NESTJS_STREAM = "spring-nestjs-requests";
    private static final String NESTJS_TO_SPRING_STREAM = "nestjs-spring-responses";
    private static final String NESTJS_CONSUMER_GROUP = "nest-consumer-group";    // NestJS와 동일
    private static final String NESTJS_CONSUMER_NAME = "nest-consumer";           // NestJS와 동일
    private static final String SPRING_CONSUMER_GROUP = "spring-consumer-group";  // Spring 전용
    private static final String SPRING_CONSUMER_NAME = "spring-consumer";

    @Override
    public void onMessage(MapRecord<String, String, String> message) {

        Map<String, String> value = message.getValue();

        log.info("[ Consumer 리딩중 ]:");
        log.info("[ Stream ]: {}", message.getStream());
        log.info("[ recordId ]: {}", message.getId());
        log.info("[ messageValue ]: {}", message.getValue());

        try {
            // 🔥 공통 필드 추출
            String originalRecordId = value.get("originalRecordId");
            String requestType = value.get("requestType");
            String success = value.get("success");
            String result = value.get("result");
            String processingTime = value.get("processingTime");
            String timestamp = value.get("timestamp");

            // 🔥 기본 유효성 검사
            if (success == null || requestType == null) {
                log.warn("❗ 필수 필드 누락: originalRecordId={}, requestType={}", originalRecordId, requestType);
                return;
            }

            // 🔥 성공/실패에 따른 처리
            boolean isSuccess = "true".equals(success);
            log.info("🎯 Processing response: Type={}, Success={}, OriginalId={}",
                    requestType, isSuccess, originalRecordId);

            if (isSuccess) {
                handleSuccessResponse(requestType, originalRecordId, result, processingTime, timestamp);
            } else {
                handleErrorResponse(requestType, originalRecordId, result, processingTime, timestamp);
            }

        } catch (Exception e) {
            log.error("❌ 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        } finally {
            // ACK 처리
            try {
                this.redisOperator.ackStream(SPRING_CONSUMER_GROUP, message);
                log.info("✅ ACK 완료: {}", message.getId());
            } catch (Exception e) {
                log.error("❌ ACK 실패: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 🔥 성공 응답 처리 - requestType별로 다른 로직 적용 가능
     */
    private void handleSuccessResponse(String requestType, String originalRecordId,
                                       String result, String processingTime, String timestamp) {
        log.info("[ requestType ]: {}", requestType);

        try {
            switch (requestType) {
                case "TEST_REQUEST" -> {
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("🧪 테스트 요청 성공");
                    log.info("   - 결과: {}", resultData);
                    log.info("   - 처리시간: {}ms", processingTime);
                    fcmService.sendMessageToUser("202210777", "알림", "보낸다");
                    log.info("[ 알림 전송 성공 ]");
                }
                case "REGISTER_USER" -> {
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("학생 회원가입 및 조직 신청");
                    String userId = resultData.get("userId").toString();
                    log.info("   - 사용자ID: {}", userId);
                    log.info("   - 상태: {}", resultData.get("status"));
                    //        message: '입금 항목이 성공적으로 추가되었습니다',
//                    fcmService.sendMessageToStudents(userId + "님", resultData.get("message").toString());
                    fcmService.sendMessageToUser(userId, userId + " 님", "회원가입 및 조직 가입 요청이 신청되었습니다.");
                    fcmService.sendMessageToCouncil("조직 가입 요청이 있습니다.", "학생 " + userId);
                }
                case "APPROVE_MEMBERSHIP" -> {
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("✅ 멤버십 승인 완료");
                    log.info("   - 결과: {}", resultData);

                    Object requestObj = resultData.get("request");
                    log.info("   - request: {}", requestObj);

                    if (requestObj instanceof Map<?, ?> requestMap) {
                        String applicantId = requestMap.get("applicantId").toString();
                        log.info("어디가 찍히는????");
                        fcmService.sendMessageToUser(applicantId, applicantId + " 님", "조직 가입 요청이 승인되었습니다.");
                    } else {
                        log.warn("[ request 형식을 알 수 없음 ]: {}", requestObj.getClass().getName());
                    }
                }
                case "REJECT_MEMBERSHIP" -> {
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("❌ 멤버십 거절 완료");
                    log.info("   - 결과: {}", resultData);
                    Object requestObj = resultData.get("request");
                    log.info("   - request: {}", requestObj);

                    if (requestObj instanceof Map<?, ?> requestMap) {
                        String rejectorId = requestMap.get("rejectorId").toString();
                        log.info("어디가 찍히는");
                        fcmService.sendMessageToUser(rejectorId, rejectorId + " 님", "조직 가입 요청이 승인되었습니다.");
                    } else {
                        log.warn("[ request 형식을 알 수 없음 ]: {}", requestObj.getClass().getName());
                    }
                }


                case "ADD_DEPOSIT" -> {
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("학생 입금 내역 요청");
                    log.info("   - 결과: {}", resultData);
                    String ledgerEntryId = resultData.get("ledgerEntryId").toString();
                    log.info("   - ledgerEntryId: {}", ledgerEntryId);
                    String userId = splitId(ledgerEntryId);
                    log.info("   - userId: {}", userId);

                    fcmService.sendMessageToUser(userId, userId + " 님", "입금 내역 등록이 신청되었습니다.");
                    fcmService.sendMessageToCouncil("입금 내역 등록 신청이 있습니다.", "학생 " + userId);
                }
                case "APPROVE_DEPOSIT" -> {
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("학생회가 입금 내역 승인");
                    log.info("   - 결과: {}", resultData);
                    String message = resultData.get("message").toString();
                    log.info("   - message: {}", message);
                    String ledgerEntryId = splitMessage(message);
                    log.info("   - ledgerEntryId: {}", ledgerEntryId);

                    String userId = splitId(ledgerEntryId);
                    log.info("   - userId: {}", userId);

                    //여기 userId가 학생인지 학생회인지
                    fcmService.sendMessageToUser(userId, userId + " 님", "입금 내역 등록이 완료되었습니다.");
                    fcmService.sendMessageToCouncil("입금 내역 등록 신청이 있습니다.", "학생 " + userId);
                }
                case "REJECT_DEPOSIT" -> {
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("학생회가 입금 내역 거절");
                    log.info("   - 결과: {}", resultData);
                    String message = resultData.get("message").toString();
                    log.info("   - message: {}", message);
                    String ledgerEntryId = splitMessage(message);
                    log.info("   - ledgerEntryId: {}", ledgerEntryId);

                    String userId = splitId(ledgerEntryId);
                    log.info("   - userId: {}", userId);

                    //여기 userId가 학생인지 학생회인지
                    fcmService.sendMessageToUser(userId, userId + " 님", "입금 내역 등록이 완료되었습니다.");
                    fcmService.sendMessageToCouncil("입금 내역 등록 신청이 있습니다.", "학생 " + userId);
                }


                case "ADD_WITHDRAW" -> {
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("학생회 출금 기입 요청");
                    log.info("   - 결과: {}", resultData);
                    String ledgerEntryId = resultData.get("ledgerEntryId").toString();
                    log.info("   - ledgerEntryId: {}", ledgerEntryId);
                    String userId = splitId(ledgerEntryId);
                    log.info("   - userId: {}", userId);

                    fcmService.sendMessageToUser(userId, userId + " 님", "출금 내역 등록이 신청되었습니다.");
                    fcmService.sendMessageToStudents("학생회 " + userId + " 님의", "출금 내역 등록 신청이 있습니다.");
//                    fcmService.sendMessageToCouncil("학생회 " + userId + " 님의", "출금 내역 등록 신청이 있습니다.");
                }
                case "VOTE_WITHDRAW" -> {
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("학생이 출금 승인 투표");
                    log.info("   - 결과: {}", resultData);
                    String message = resultData.get("message").toString();
                    log.info("   - message: {}", message);
                    String ledgerEntryId = splitMessage(message);
                    log.info("   - ledgerEntryId: {}", ledgerEntryId);

                    String userId = splitId(ledgerEntryId);
                    log.info("   - userId: {}", userId);
                    //투표를 만든 사람한테 알림, 투표를 한 개개인을 데이터에 안 담아서 어쩔 수 없음~~~
                    fcmService.sendMessageToUser(userId, userId + " 님", "출금 내역 투표가 있습니다.");
                    //투표는 결과를 보여주는 알림이 있으면 좋겠음
                    //예) title:모군님, body:학생회 00출금 내역 투표 종료 10분 전입니다
                    //title:학생회 00 출금 내역의 투표 결과입니다. body: 찬성 10명, 반대 15명
                }



                case "GET_STUDENT_COUNT", "GET_COUNCIL_COUNT" -> {
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("📊 조회 요청 완료: {}", requestType);
                    log.info("   - 결과: {}", resultData);
                    String count = resultData.get("result").toString();
                    log.info("   - count: {}", count);

                    requestTracker.markRequestCompleted(originalRecordId);
                    log.info("🎯 학생 수 요청 완료 처리: {}", originalRecordId);
                }
                case "GET_PENDING_REQUESTS" -> {
                    log.info("대기중인 요청 조회 완료");
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("   - 결과: {}", resultData);

                    requestTracker.markRequestCompleted(originalRecordId);
                    log.info("🎯 대기중인 요청 조회 완료 처리: {}", originalRecordId);
                }
                case "GET_REQUEST_STATUS" -> {
                    log.info("특정 id 상태 조회 완료");
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("조회 요청 완료: {}", requestType);
                    log.info("   - 결과: {}", resultData);
                    String status = resultData.get("status").toString();
                    log.info("   - status: {}", status);

                    requestTracker.markRequestCompleted(originalRecordId);
                    log.info("🎯 학생 수 요청 완료 처리: {}", originalRecordId);
                }
                case "GET_PENDING_DEPOSITS" -> {
                    log.info("GET_PENDING_DEPOSITS");
                    List<Map<String, Object>> depositList = objectMapper.readValue(
                            result, new TypeReference<List<Map<String, Object>>>() {});
                    log.info("   - 결과: {}", depositList);

                    requestTracker.markRequestCompleted(originalRecordId);
                    log.info("🎯 GET_PENDING_DEPOSITS 완료 처리: {}", originalRecordId);
                }
                case "GET_PENDING_WITHDRAW" -> {
                    log.info("GET_PENDING_WITHDRAW");
                    List<Map<String, Object>> depositList = objectMapper.readValue(
                            result, new TypeReference<List<Map<String, Object>>>() {});
                    log.info("   - 결과: {}", depositList);

                    requestTracker.markRequestCompleted(originalRecordId);
                    log.info("🎯 GET_PENDING_WITHDRAW 완료 처리: {}", originalRecordId);
                }
                case "GET_VOTE_STATUS" -> {
                    log.info("GET_VOTE_STATUS");
                    Map<String, Object> resultData = objectMapper.readValue(result, Map.class);
                    log.info("조회 요청 완료: {}", requestType);
                    log.info("   - 결과: {}", resultData);
//                    String status = resultData.get("status").toString();
//                    log.info("   - status: {}", status);
                    requestTracker.markRequestCompleted(originalRecordId);
                    log.info("🎯 투표 진행 조회 완료 처리: {}", originalRecordId);

                    //
                }
                case "GET_THEME_BALANCE" -> {
                    log.info("GET_THEME_BALANCE");
                }
                case "GET_ALL_THEME_BALANCE" -> {
                    log.info("GET_ALL_THEME_BALANCE");
                }

                default -> {
                    log.info("🔄 일반 요청 처리 완료: {}", requestType);
//                    log.info("   - 결과: {}", resultData);
                }
            }

        } catch (Exception e) {
            log.warn("⚠️ result JSON 파싱 실패, 원본 그대로 사용: {}", result);
            // 그래도 latch는 풀어줘야 서비스가 안 죽음
//            requestTracker.markRequestCompleted(originalRecordId);
        }
    }

    private String splitMessage(String message) {
        String[] s = message.split(" ");
        return s[2];
    }

    private String splitId(String ledgerEntryId) {
        String[] s = ledgerEntryId.split("_");
        return s[2];
    }

    private void handleErrorResponse(String requestType, String originalRecordId,
                                     String error, String processingTime, String timestamp) {
        log.error("❌ 요청 처리 실패: Type={}, Error={}", requestType, error);
        log.error("   - 원본 RecordId: {}", originalRecordId);
        log.error("   - 처리시간: {}ms", processingTime);
    }


    @Override
    public void destroy() throws Exception {
        if(this.subscription != null){
            this.subscription.cancel();
        }
        if(this.listenerContainer != null){
            this.listenerContainer .stop();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // Consumer Group 설정
        this.redisOperator.createStreamConsumerGroup(NESTJS_TO_SPRING_STREAM, SPRING_CONSUMER_GROUP);

        // StreamMessageListenerContainer 설정
        this.listenerContainer = this.redisOperator.createStreamMessageListenerContainer();

        //Subscription 설정
        this.subscription = this.listenerContainer.receive(
                Consumer.from(this.SPRING_CONSUMER_GROUP, SPRING_CONSUMER_NAME), //누가 읽을지
                StreamOffset.create(NESTJS_TO_SPRING_STREAM, ReadOffset.lastConsumed()),
                //어떤 스트림을 어디서부터 읽을지
                this
        );

        log.info("streamKey: {}", NESTJS_TO_SPRING_STREAM);

        // 2초 마다, 정보 GET
        this.subscription.await(Duration.ofSeconds(2));

        // redis listen 시작
        this.listenerContainer.start();
    }
}
