package Baeksa.money.domain.streams;


import Baeksa.money.domain.auth.Service.MemberService;
import Baeksa.money.domain.auth.Service.StudentValidService;
import Baeksa.money.domain.auth.converter.MemberConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamsService {
    
    private final MemberService memberService;
    private final StudentValidService studentValidService;
    private final MemberConverter memberConverter;
    private final StreamsProducer streamProducer;

//    public MemberDto.MemberResponseDto signup(MemberDto memberDto) {
//
//        String studentId = memberDto.getStudentId();
//        String username = memberDto.getUsername();
//        String phoneNumber = memberDto.getPhoneNumber();
//        Role role = memberDto.getRole();
//
//        try {
//            boolean ValidStudent = studentValidService.signupValid(
//                    studentId, username, phoneNumber, role);
//            //아닐경우 학생정보 찾을 수 없다는 커스텀에러
//            if (!ValidStudent) {
//                throw new CustomException(ErrorCode.STUDENT_NOTFOUND);
//            }
//            //회원가입 서비스 호출 - 여기서 중복회원, 비밀번호 2차
//            MemberEntity savedEntity = memberService.signup(memberDto);
//
//            //savedEntity를 MemberDto로 변환하여 반환해야됨(컨트롤러니깐)
//            MemberDto.MemberResponseDto savedDto = memberConverter.toResponseDto(savedEntity);
//            log.info("savedDto: {}", savedDto.getStudentId());
//            log.info("memberDto: {}", memberDto.getStudentId());
//
//            streamProducer.publishSignup(studentId, username, role);
////            streamProducer.publishSignup2(studentId, username, role);
//
//            return savedDto;
//
//            // 커스텀 예외는 그대로 던져서 글로벌 예외처리기에서 처리되게
//        } catch (CustomException e) {
//            throw e;
//
//        } catch (Exception e) {
//            throw new CustomException(ErrorCode.INTERNAL_SERVER);
//        }
//    }


    public void test(String message) {
        streamProducer.publishSignup2(message);
    }
}