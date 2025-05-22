package Baeksa.money.domain.streams.controller;

//import Baeksa.money.domain.streams.service.StreamClaudeService;
import Baeksa.money.domain.streams.dto.StreamReqDto;
import Baeksa.money.domain.streams.service.github.RedisStreamProducer;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import Baeksa.money.global.excepction.code.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Slf4j
@RestController
@RequestMapping("/streams")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입 & 로그인 관련 API")
public class StreamsController {

    private final RedisStreamProducer redisStreamProducer;

//    @Operation(summary = "stream Test 회원가입 API")
//    @ApiErrorCodeExample(
//            value = ErrorCode.class,
//            include = {"STUDENT_NOTFOUND", "DUPLICATED_MEMBER", "DUPLICATED_MEMBER", "PASSWORD_NOT_MATCH", "INVALID_ACCESS"}
//    )
//    @PostMapping("/signup2")
//    public ResponseEntity<?> signup(@Valid @RequestBody MemberDto memberDto) {
//
//        MemberDto.MemberResponseDto savedDto = streamsService.signup(memberDto);
//        return ResponseEntity.ok(new BaseApiResponse<>(201, "SIGNUP", "회원가입 완료", savedDto));
//    }


//    @PostMapping("/signup-test")
//    public ResponseEntity<?> test(@RequestBody StreamReqDto.StreamTestDto dto) {
//        RecordId recordId = streamsProducer.testAddMessage(dto);
//        return ResponseEntity.ok(new BaseApiResponse<>(200, "test", "test 완료", recordId));
//    }

    @PostMapping("/signup-test")
    public ResponseEntity<?> test(@RequestBody StreamReqDto.StreamTestDto dto) {
        RecordId recordId = redisStreamProducer.sendMessage(dto);
        return ResponseEntity.ok(new BaseApiResponse<>(200, "test", "test 완료", recordId.getValue()));
    }

//    @PostMapping("/process")
//    public ResponseEntity<?> processRequest(@RequestBody StreamReqDto.StreamTestDto dto) {
//        try {
//            // Nest.js 서비스에 요청 전송 및 응답 대기
//            CompletableFuture<StreamReqDto.StreamTestDto> responseFuture =
//                    streamService.processRequest(dto, new TypeReference<StreamReqDto.StreamTestDto>() {});
//            // 비동기 응답 처리
//            StreamReqDto.StreamTestDto response = responseFuture.get(30, TimeUnit.SECONDS);
//            return ResponseEntity.ok(new BaseApiResponse<>(200, "Success", "Request processed successfully", response));
//
//        } catch (Exception e) {
//            throw new CustomException(ErrorCode.STREAMS_SEND_FAIL);
//        }
//    }
//
//    // 비동기 버전 (요청을 즉시 수락하고 나중에 결과 확인)
//    @PostMapping("/process-async")
//    public ResponseEntity<?> processRequestAsync(@RequestBody Map<String, Object> requestBody) {
//        try {
//            // 요청 ID 생성
//            String requestId = UUID.randomUUID().toString();
//
//            // 비동기적으로 요청 처리
//            streamService.processRequest(requestBody, new TypeReference<Map<String, Object>>() {
//                    })
//                    .thenAccept(response -> {
//                        log.info("✅ Async request {} completed with response: {}", requestId, response);
//                        // 여기에서 웹소켓이나 서버-센트 이벤트로 클라이언트에게 알림을 보낼 수 있음
//                    })
//                    .exceptionally(ex -> {
//                        log.error("❌ Async request {} failed", requestId, ex);
//                        return null;
//                    });
//
//            // 즉시 요청 수락 응답
//            return ResponseEntity.accepted()
//                    .body(new BaseApiResponse<>(202, "Accepted", "Request accepted for processing", Map.of("requestId", requestId)));
//
//        } catch (Exception e) {
//            log.error("Failed to accept request", e);
//            throw new CustomException(ErrorCode.STREAMS_SEND_FAIL);
//        }
//    }

}