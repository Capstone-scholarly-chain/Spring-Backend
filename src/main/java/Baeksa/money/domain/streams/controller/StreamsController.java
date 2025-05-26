package Baeksa.money.domain.streams.controller;

//import Baeksa.money.domain.streams.service.StreamClaudeService;
import Baeksa.money.domain.streams.dto.StreamReqDto;
import Baeksa.money.domain.streams.service.RedisStreamProducer;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/streams")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입 & 로그인 관련 API")
public class StreamsController {

    private final RedisStreamProducer redisStreamProducer;


    @PostMapping("/signup-test")
    public ResponseEntity<?> test(@RequestBody StreamReqDto.StreamTestDto dto) {
        RecordId recordId = redisStreamProducer.sendMessage(dto, "TEST_REQUEST");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "test", "test 완료", recordId.getValue()));
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approve(@RequestBody StreamReqDto.MembershipApprovalDto dto) {
        RecordId recordId = redisStreamProducer.sendMessage(dto, "APPROVE_MEMBERSHIP");
        return ResponseEntity.ok(new BaseApiResponse<>(200, "test", "test 완료", recordId.getValue()));
    }
}