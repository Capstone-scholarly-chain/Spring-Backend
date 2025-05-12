package Baeksa.money.domain.committee.service;

import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.RedisDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class CommitteeSubscriber implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final CommitteePubSubService committeePubSubService;

    // NestJS 응답 채널 정의
    private static final class ResponseChannels {
        // 기존 채널들
        public static final String TEST_PUBLISHER_REQUEST = "test.publisher.request";
        public static final String REGISTER_USER = "nestjs:response:register-user";
        public static final String MEMBERSHIP_UPDATED = "nestjs:response:membership:updated";
        public static final String MEMBERSHIP_APPROVE = "nestjs:response:membership:approve";
        public static final String MEMBERSHIP_REJECTED = "nestjs:response:membership:rejected";
    }

        // 핸들러 맵: 채널 이름 → 처리 로직
    private final Map<String, Consumer<Map<String, Object>>> handlerMap = new HashMap<>();

    public CommitteeSubscriber(ObjectMapper objectMapper,
                             RedisTemplate<String, Object> redisTemplate, CommitteePubSubService committeePubSubService) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.committeePubSubService = committeePubSubService;


        // 메시지 핸들러 등록
        initializeHandlers();
    }

    private void initializeHandlers() {
        // 멤버십 관련 핸들러
        handlerMap.put(ResponseChannels.REGISTER_USER, this::handleRegisterUser);
        handlerMap.put(ResponseChannels.MEMBERSHIP_UPDATED, this::handleMembershipUpdated);
        handlerMap.put(ResponseChannels.MEMBERSHIP_APPROVE, this::handleMemberShipApprove);
        handlerMap.put(ResponseChannels.MEMBERSHIP_REJECTED, this::handleMembershipReject);
        handlerMap.put(ResponseChannels.TEST_PUBLISHER_REQUEST, this::handleTest);


//        // 조회 관련 핸들러
//        handlerMap.put(ResponseChannels.STUDENT_COUNT, this::handleStudentCount);
//        handlerMap.put(ResponseChannels.STUDENT_COUNCIL_COUNT, this::handleStudentCouncilCount);
//        handlerMap.put(ResponseChannels.PENDING_REGISTER, this::handlePendingRegister);
//        handlerMap.put(ResponseChannels.REGISTER_STATUS, this::handleRegisterUserStatus);
//        handlerMap.put(ResponseChannels.PENDING_DEPOSITS, this::handlePendingDeposits);
//        handlerMap.put(ResponseChannels.PENDING_WITHDRAWS, this::handlePendingWithdraws);
//        handlerMap.put(ResponseChannels.WITHDRAWS_VOTE_STATUS, this::handleWithdrawVoteStatus);
//        handlerMap.put(ResponseChannels.THEMA_BALANCE, this::handleThemaBalance);
//        handlerMap.put(ResponseChannels.THEMA_BALANCES, this::handleThemaBalances);

        log.info("Redis 메시지 핸들러 초기화 완료 - 총 {} 개의 채널 등록됨", handlerMap.size());
        log.info("handlerMap", handlerMap.size());

    }

    private void handleTest(Map<String, Object> msg) {
        log.info("테스트 요청:{}", msg);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {
//            String publishMessage = redisTemplate.getStringSerializer().deserialize(message.getBody());
            //이건 onMessage의 파라미터 message야, pattern으로 채널 뽑을 수 있음
            String publishMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            String channel = new String(pattern, StandardCharsets.UTF_8);

            log.info("Redis 메시지 수신 - 채널: {}, 메시지: {}", channel, publishMessage);

            // JSON 메시지를 Map으로 변환
            Map<String, Object> messageMap = objectMapper.readValue(publishMessage,
                    new TypeReference<Map<String, Object>>() {});

            // 채널별 처리 핸들러 실행
            Consumer<Map<String, Object>> handler = handlerMap.get(channel);
            if (handler != null) {
                handler.accept(messageMap);
            } else {
//                // eventType을 통한 대체 채널 식별 시도
//                String eventType = (String) messageMap.get("eventType");
//                if (eventType != null) {
//                    log.info("채널 대신 eventType으로 핸들러 식별 시도: {}", eventType);
//                    handler = getHandlerByEventType(eventType);
//                    if (handler != null) {
//                        handler.accept(messageMap);
//                        return;
//                    }
//                }
                throw new CustomException(ErrorCode.NO_CHANNEL);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }
//    // eventType을 기반으로 핸들러 반환
//    private Consumer<Map<String, Object>> getHandlerByEventType(String eventType) {
//        switch (eventType) {
//            case "TEST_PUBLISHER_REQUEST":
//                return this::handleTest;
//            // 다른 이벤트 타입에 대한 핸들러 매핑 추가
//            default:
//                return null;
//        }
//    }

    //    private List<String> handleRegisterUser(Map<String, Object> msg) {
//        List<String> students = studentPubSubService.getStudents();
//        log.info("학생 가입 요청");
//        return students;
//    }
    private void handleRegisterUser(Map<String, Object> msg) {
        log.info("학생 가입 요청: {}", msg);
        committeePubSubService.processRegisterUser(msg);
    }

    private void handleMembershipUpdated(Map<String, Object> msg) {
        log.info("학생 요청 실패: {}", msg);
        committeePubSubService.processMembershipUpdate(msg);
    }

    private void handleMemberShipApprove(Map<String, Object> msg) {
        log.info("학생 요청 승인: {}", msg);
        committeePubSubService.processMembershipApproval(msg);
    }

    private void handleMembershipReject(Map<String, Object> msg) {
        log.info("학생 요청 거절: {}", msg);
        committeePubSubService.processMembershipRejection(msg);
    }
}