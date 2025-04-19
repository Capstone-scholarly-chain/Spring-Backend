package Baeksa.money.domain.Controller;


import Baeksa.money.domain.Dto.MemberDto;
import Baeksa.money.domain.Entity.MemberEntity;
import Baeksa.money.domain.Service.MemberService;
import Baeksa.money.domain.Service.StudentService;
import Baeksa.money.domain.converter.MemberConverter;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.ErrorCode;
import Baeksa.money.global.redis.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;


//회원가입 & 검증용 컨트롤러
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "회원가입 & 로그인 관련 API")
public class AuthController {

    private final MemberService memberService;
    private final StudentService studentService;
    private final MemberConverter memberConverter;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthController(MemberService memberService, StudentService studentService,
                          MemberConverter memberConverter, RefreshTokenService refreshTokenService) {
        this.memberService = memberService;
        this.studentService = studentService;
        this.memberConverter = memberConverter;
        this.refreshTokenService = refreshTokenService;
    }

//    @GetMapping("/signup")
//    public String signup() {
//        return "signup";
//    }

    @Operation(description = "회원가입 API")
    @PostMapping("/signup")
    public ResponseEntity<MemberDto.MemberResponseDto> signup(@Valid @RequestBody MemberDto memberDto) {

        //학생정보가 맞는지 studentService에서 검증 로직 실행
        try{
            boolean ValidStudent = studentService.signupValid(
                    memberDto.getStudentId(),
                    memberDto.getUsername(),
                    memberDto.getPhoneNumber(),
                    memberDto.getRole()
            );
            //아닐경우 학생정보 찾을 수 없다는 커스텀에러
            if (!ValidStudent) {
                throw new CustomException(ErrorCode.STUDENT_NOTFOUND);
            }

            //회원가입 서비스 호출
            MemberEntity savedEntity = memberService.signup(memberDto);

            //savedEntity를 MemberDto로 변환하여 반환해야됨(컨트롤러니깐)
            MemberDto.MemberResponseDto savedDto = memberConverter.toResponseDto(savedEntity); // ✅ OK

            //가입된 회원의 정보를 반환
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);

            // 커스텀 예외는 그대로 던져서 글로벌 예외처리기에서 처리되게
        } catch (CustomException e) {
            throw e;

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER);
        }
    }

//    @GetMapping("/login")
//    public String login() {
//        return "login";
//    }

    // 스프링 시큐리티, jwt token으로 로그인 방식 변경함
//    @PostMapping("/login")
//    public ResponseEntity<Map<String, String>> login(@RequestBody MemberDto memberDto) {
//        Map<String, String> response = new HashMap<>();
//
//        // 학번 검증
//        boolean validId = memberService.isValidStudentId(memberDto.getStudentId());
//        if (!validId) {
////            return "redirect:/login";  // 학번이 유효하지 않으면 로그인 페이지로 리다이렉트
//            response.put("error", "사용자를 찾을 수 없습니다.");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//
//        // 비밀번호 검증
//        boolean validPassword = memberService.isValidPassword(memberDto.getStudentId(), memberDto.getPassword());
//        if (!validPassword) {
////            return "redirect:/login";  // 비밀번호가 틀리면 로그인 페이지로 리다이렉트
//            response.put("error", "비밀번호가 일치하지 않습니다.");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//        }
//
//        // 학생회 여부 검증
//        boolean validCommittee = memberService.isValidCommittee(memberDto.getStudentId(), memberDto.isCommittee());
//        if (!validCommittee) {
////            return "redirect:/login";  // 학생회 여부가 맞지 않으면 로그인 페이지로 리다이렉트
//            response.put("error", "학생회 여부가 올바르지 않습니다.");
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//        }
//
////        // 모든 검증이 통과하면 메인 페이지로 리다이렉트
////        return "redirect:/main";
//
//        // 모든 검증이 통과하면 성공 응답 반환
//        response.put("message", "로그인 성공");
//        response.put("redirectUrl", "/login"); // 프론트에서 알아서 처리하도록 URL 전달
//        return ResponseEntity.ok(response);
//    }

    @Operation(description = "refresh토큰 재발행")
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        MemberDto.TokenResponse tokenResponse = refreshTokenService.refreshValid(request);
        return refreshTokenService.reissue(response, tokenResponse);
    }

    @Operation(description = "로그아웃 API")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        //로그아웃 전에 refresh 검증: reissue랑 똑같음
        //refresh꺼내서 null check / isExpired / category / DB저장 확인
        MemberDto.TokenResponse tokenResponse = refreshTokenService.refreshValid(request);

        //해당 refresh토큰 삭제
        refreshTokenService.logout(tokenResponse.getStudentId());

        // access 쿠키 삭제
        Cookie accessCookie = new Cookie("access", null);
        accessCookie.setMaxAge(0);  // 쿠키 만료 시간 0으로 설정 (삭제)
        accessCookie.setPath("/");  // 경로를 루트로 설정
        response.addCookie(accessCookie);

        // refresh 쿠키 삭제
        Cookie refreshCookie = new Cookie("refresh", null);
        refreshCookie.setMaxAge(0);  // 쿠키 만료 시간 0으로 설정 (삭제)
        refreshCookie.setPath("/");  // 경로를 루트로 설정
        response.addCookie(refreshCookie);

        return ResponseEntity.ok("로그아웃");
    }

}
