package Baeksa.money.domain.error;

import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.redis.RedisDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class ErrorSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final Map<String, Consumer<Map<String, Object>>> requestTypeHandlerMap = new HashMap<>();

    public ErrorSubscriber(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;


        // 요청 타입별 에러 처리 핸들러 등록
        requestTypeHandlerMap.put("REGISTER", this::handleRegisterError);
        requestTypeHandlerMap.put("MEMBERSHIP_APPROVE", this::handleMembershipApproveError);
        requestTypeHandlerMap.put("MEMBERSHIP_REJECT", this::handleMembershipRejectError);
        requestTypeHandlerMap.put("PENDING_MEMBERSHIP", this::handleMembershipPendingError);
        requestTypeHandlerMap.put("GET_MEMBERSHIP_STATUS", this::handleMemberShipStatusError);
        requestTypeHandlerMap.put("STUDENT_COUNT", this::handleStudentCountError);
        requestTypeHandlerMap.put("STUDENT_COUNCIL_COUNT", this::handleCouncilCountError);
        requestTypeHandlerMap.put("ADD_DEPOSIT", this::handleAddDepositError);
        requestTypeHandlerMap.put("ADD_WITHDRAW", this::handleAddWithdrawError);
        requestTypeHandlerMap.put("DEPOSIT_APPROVE", this::handleDepositApproveError);
        requestTypeHandlerMap.put("DEPOSIT_REJECT", this::handleDepositRejectError);
        requestTypeHandlerMap.put("WITHDRAW_VOTE", this::handleWithdrawVoteError);
        requestTypeHandlerMap.put("PENDING_DEPOSITS", this::handlePendingDepositsError);
        requestTypeHandlerMap.put("PENDING_WITHDRAWS", this::handlePendingWithdrawsError);
        requestTypeHandlerMap.put("GET_WITHDRAW_STATUS", this::handleWithdrawStatusError);
        requestTypeHandlerMap.put("GET_ONE_AMOUNTS", this::handleOneAmountsError);
        requestTypeHandlerMap.put("GET_AMOUNTS", this::handleAmountsError);
        requestTypeHandlerMap.put("GET_ONE_THEME_ITEMS", this::handleOneThemeItemsError);
        requestTypeHandlerMap.put("GET_ALL_THEME_ITEMS", this::handleAllThemeItemsError);

        // 필요 시 추가 가능
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            RedisDto.MessageDto redisDto = objectMapper.readValue(publishMessage, RedisDto.MessageDto.class);

            String channel = redisDto.getChannel();
            Map<String, Object> msg = redisDto.getMessage();

            log.info("🔔 Redis Subscribe Channel : {}", channel);
            log.info("📨 Redis SUB Message : {}", publishMessage);

            if (!"nestjs:response:error".equals(channel)) {
                log.warn("등록되지 않은 에러 채널: {}", channel);
                return;
            }

            String requestType = (String) msg.get("requestType");
            Consumer<Map<String, Object>> handler = requestTypeHandlerMap.get(requestType);

            if (handler != null) {
                handler.accept(msg);
            } else {
                log.warn("정의되지 않은 requestType: {}", requestType);
            }

        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }

    private void handleRegisterError(Map<String, Object> msg) {
        String userId = (String) msg.get("userId");
        String errorMessage = (String) msg.get("error");

        //해당 userId에게 fcm알림을 보내기
        log.error("회원가입 실패 - userId: {}, error: {}", userId, errorMessage);
        throw new CustomException(ErrorCode.REGISTER_REQUEST_FAILED);
    }

    private void handleMembershipApproveError(Map<String, Object> msg) {
        String requestId = (String) msg.get("requestId");
        String approverId = (String) msg.get("approverId");
        String errorMessage = (String) msg.get("error");

        //requestId에서 studentId랑 approverId fcm 보내기
        log.error("가입 승인 실패 - requestId: {}, approverId: {}, error: {}",
                requestId, approverId, errorMessage);
        throw new CustomException(ErrorCode.MEMBERSHIP_APPROVE_REQUEST_FAILED);
    }

    private void handleMembershipRejectError(Map<String, Object> stringObjectMap) {

        throw new CustomException(ErrorCode.MEMBERSHIP_REJECT_REQUEST_FAILED);
    }

    private void handleStudentCountError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.STUDENT_COUNT_REQUEST_FAILED);
    }

    private void handleCouncilCountError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.COUNCIL_COUNT_REQUEST_FAILED);
    }

    private void handleAddDepositError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.ADD_DEPOSIT_REQUEST_FAILED);
    }

    private void handleAddWithdrawError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.ADD_WITHDRAW_REQUEST_FAILED);
    }

    private void handleDepositApproveError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.DEPOSIT_APPROVE_REQUEST_FAILED);
    }

    private void handleDepositRejectError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.DEPOSIT_REJECT_REQUEST_FAILED);
    }

    private void handleMemberShipStatusError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.MEMBERSHIP_STATUS_REQUEST_FAILED);
    }

    private void handleMembershipPendingError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.MEMBERSHIP_PENDING_REQUEST_FAILED);
    }

    private void handleWithdrawVoteError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.WITHDRAW_VOTE_REQUEST_FAILED);
    }

    private void handlePendingDepositsError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.PENDING_DEPOSITS_REQUEST_FAILED);
    }

    private void handlePendingWithdrawsError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.PENDING_WITHDRAWS_REQUEST_FAILED);
    }

    private void handleWithdrawStatusError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.WITHDRAW_STATUS_REQUEST_FAILED);
    }

    private void handleOneAmountsError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.ONE_AMOUNTS_REQUEST_FAILED);
    }

    private void handleAmountsError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.AMOUNTS_REQUEST_FAILED);
    }

    private void handleOneThemeItemsError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.ONE_THEME_ITEMS_REQUEST_FAILED);
    }

    private void handleAllThemeItemsError(Map<String, Object> stringObjectMap) {
        throw new CustomException(ErrorCode.ALL_THEME_ITEMS_REQUEST_FAILED);
    }
}

