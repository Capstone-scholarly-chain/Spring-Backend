package Baeksa.money.domain.auth.Service;


import Baeksa.money.domain.auth.Dto.MemberDto;
import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.Repository.MemberRepository;
import Baeksa.money.domain.auth.converter.MemberConverter;
import Baeksa.money.domain.auth.enums.Role;
import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberConverter memberConverter;
    private final JWTUtil jwtUtil;

    public MemberEntity findById(String id) {
        return memberRepository.findByStudentId(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOTFOUND));
    }

    public MemberEntity signup(MemberDto memberDto) {

        //회원가입 시 Student 데이터 검증 - 이름/학번/학생회여부
        //는 StudentService에서 처리한다.

        //회원가입 시작
        //1. 중복 가입 방지, 학번으로 가입여부 확인
        if (memberRepository.existsByStudentId(memberDto.getStudentId())) {
            throw new CustomException(ErrorCode.DUPICATED_MEMBER);
        }

        //2. 비밀번호 확인
        boolean equals = memberDto.getPassword().equals(memberDto.getConfirmPassword());
        if (!equals) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        //3. 비밀번호 암호화는 컨버터에서 해주게끔 했다
        MemberEntity memberEntity = memberConverter.toEntity(memberDto);

        System.out.println(memberDto.getStatus());

        //5. 저장
        return memberRepository.save(memberEntity);
    }


    public void ValidAccess(String header){

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // "Bearer " 이후만 추출
            // token 검증 로직 수행
            jwtUtil.validateJwt(token);

        } else {
            throw new CustomException(ErrorCode.INVALID_ACCESS);
        }
    }

    @Transactional
    public void approve(String studentId) {
        Optional<MemberEntity> Op = memberRepository.findByStudentId(studentId);
        MemberEntity member = Op.get();
        member.approve();
    }

    @Transactional
    public void reject(String studentId) {
        Optional<MemberEntity> Op = memberRepository.findByStudentId(studentId);
        MemberEntity member = Op.get();
        member.reject();
    }

    public List<String> getUserIdsByRole(Role role){
        return memberRepository.findUserIdsByRole(role);
    }
}
