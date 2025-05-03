package Baeksa.money.global.redis;

import Baeksa.money.domain.Dto.MemberDto;
import Baeksa.money.domain.Entity.MemberEntity;
import Baeksa.money.domain.Service.MemberService;
import Baeksa.money.domain.enums.Status;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.ErrorCode;
import Baeksa.money.global.jwt.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/pubsub/request")
@Tag(name = "이벤트", description = "학생 회원 등록/장부 등록 API")
@RequiredArgsConstructor
public class RedisController {

    private final RedisPublisher redisPublisher;
    private final MemberService memberService;


    @Operation(description = "학생 가입 신청 - 학생 화면")
    @PostMapping("/membership")
    public ResponseEntity membership(@RequestParam("agree") boolean agree,
                           @RequestHeader("Authorization") String authToken,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {

        memberService.ValidAccess(authToken);

        if(agree){
            MemberEntity byId = memberService.findById(userDetails.getStudentId());
            MemberDto dto = MemberDto.builder()
                    .studentId(byId.getStudentId())
                    .username(byId.getUsername())
                    .role(byId.getRole())
                    .timestamp(byId.getTimestamp())
                    .build();


            try {
                Map map = new HashMap<String, Object>();
                map.put("studentId", dto.getStudentId());
                map.put("applicantId", dto.getStudentId());
                map.put("username", dto.getUsername());
                map.put("role", dto.getRole().toString());
                map.put("timestamp", dto.getTimestamp().toString());

                ObjectMapper mapper = new ObjectMapper();
                String message = mapper.writeValueAsString(map);
                System.out.println(message);
//            ObjectNode message = mapper.createObjectNode();
//            message.put("studentId", dto.getStudentId());
//            message.put("username", dto.getUsername());
//            message.put("role", dto.getRole().toString());
//            message.put("timestamp", dto.getTimestamp().toString());

                redisPublisher.publish("spring:request:membership", message);
                return ResponseEntity.ok(new MemberDto.ApiResponse<>(200, "PUBSUB-MEMBERSHIP", "학생 가입 신청", message));

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.badRequest().build();
    }



    //message안에 approvals이 있으면 리스트 append? 갱신
    @Operation(description = "학생 가입 승인 - 학생회화면")
    @PostMapping("/approve")
    public ResponseEntity join(@RequestBody RedisDto.pubDto redisDto,
                               @AuthenticationPrincipal CustomUserDetails userDetails){
        //학생회 정보 - userDetails
        //승인할 학생 정보 - redisDto
        Map map;
        MemberEntity byId = memberService.findById(redisDto.getApplicantId());
        MemberDto dto = MemberDto.builder()
                .id(byId.getId())
                .studentId(byId.getStudentId())
                .username(byId.getUsername())
                .role(byId.getRole())
                .status(byId.getStatus())
                .approvals(new ArrayList())
                .rejections(new ArrayList())
                .timestamp(byId.getTimestamp())
                .build();

        try {
            map = new HashMap<String, Object>();

            map.put("id", "REQ_STUDENT_" + dto.getTimestamp().toString() + "_" + dto.getStudentId());
            map.put("applicantId", dto.getStudentId());
            map.put("username", dto.getUsername());
            map.put("role", dto.getRole().toString());
            map.put("timestamp", dto.getTimestamp().toString());
            List<Long> rejections = dto.getRejections();
            List<Long> approvals = dto.getApprovals();

            //reject의 경우
            if(!redisDto.isActive()){
                if(dto.getStatus() == Status.PENDING){
                    memberService.reject(dto.getStudentId());
                    // 반대한 학번 리스트
                    rejections.add(userDetails.getStudentId());
                    dto.setRejections(rejections);
                    map.put("rejections", rejections);

                } else {
                    map.put("status", dto.getStatus().toString());
                    throw new CustomException(ErrorCode.ALREADY_STATUS);
                }

            } else {
                if(dto.getStatus() == Status.PENDING){
                    memberService.approve(dto.getStudentId());
                    // 찬성한 학번 리스트
                    approvals.add(userDetails.getStudentId());
                    dto.setApprovals(approvals);
                    map.put("approvals", approvals);

                } else {
                    map.put("status", dto.getStatus().toString());
                    throw new CustomException(ErrorCode.ALREADY_STATUS);
                }
            }

//            ObjectMapper mapper = new ObjectMapper();
//            message = mapper.writeValueAsString(map);
//            System.out.println(message);

            redisPublisher.publish("spring:request:approve", map);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        //메시지를 담고
        //access되어있는 사람들한테 반복문으로 fcm알림을 날린다던지 하면 됨

        return ResponseEntity.ok(new MemberDto.ApiResponse<>(201, "PUBSUB-APPROVE", "학생 가입 승인", map));
    }


}
