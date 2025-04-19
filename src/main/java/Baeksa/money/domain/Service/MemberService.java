package Baeksa.money.domain.Service;


import Baeksa.money.domain.Dto.MemberDto;
import Baeksa.money.domain.Entity.MemberEntity;
import Baeksa.money.domain.Repository.MemberRepository;
import Baeksa.money.domain.converter.MemberConverter;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberConverter memberConverter;

    @Autowired
    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder, MemberConverter memberConverter) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.memberConverter = memberConverter;
    }


    public MemberEntity signup(MemberDto memberDto) {

        //회원가입 시 Student 데이터 검증 - 이름/학번/학생회여부
        //는 StudentService에서 처리한다.

        //회원가입 시작
        //1. 중복 가입 방지, 학번으로 가입여부 확인
        //이미 가입한거면 IllegalArgumentException날리기
        if (memberRepository.existsByStudentId(memberDto.getStudentId())) {
            throw new CustomException(ErrorCode.DUPICATED_MEMBER);
        }

        //2. 비밀번호 확인
        boolean equals = memberDto.getPassword().equals(memberDto.getConfirmPassword());
        if (!equals) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        //3. 비밀번호 암호화는 컨버터에서 해주게끔 했다
//        String encodedPassword = bCryptPasswordEncoder.encode(memberDto.getPassword());

        //4. DTO → Entity 빌더 변환
        MemberEntity memberEntity = memberConverter.toEntity(memberDto);

        //5. 저장
        return memberRepository.save(memberEntity);
    }


///  스프링시큐리티, jwt가 로그인을 처리하므로 아래 로직별로 에러 메세지를 보내려면 예외 메세지를 커스텀하고 핸들러를 만들어야함
//    /// 로그인할 때 학번, 비밀번호, 학생여부 확인
//    public boolean isValidStudentId(Long studentId) {
//        return memberRepository.findByStudentId(studentId).isPresent();
//    }
//
//    public boolean isValidPassword(Long studentId, String password) {
//        Optional<MemberEntity> OptionalEntity = memberRepository.findByStudentId(studentId);
//        if(OptionalEntity.isEmpty()) {
//            //id에 해당하는 사용자 있는지 또 체크
//            return false;
//        }
//
//        MemberEntity student = OptionalEntity.get();
//        //비밀번호 암호화한건 matches랑 비교하기
//        return bCryptPasswordEncoder.matches(password, student.getPassword());
//        //어차피 반환형이 불린이라 if로 인지아닌지 불린 반환할 필요 없이 간단하게
//    }

}
