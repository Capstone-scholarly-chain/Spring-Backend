package Baeksa.money.global.jwt;

import Baeksa.money.domain.auth.Entity.MemberEntity;
import Baeksa.money.domain.auth.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    //함수이름은 바꿀 수 없고 매개변수만 학번으로 바꿈
    //얘는 username이 고정이라서 이걸 studentId로 파싱해서 Long으로 바꿔줘야함
    @Override
    public UserDetails loadUserByUsername(String studentId) throws UsernameNotFoundException {
//        String studentId = username;
        MemberEntity memberEntity = memberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 학번의 사용자를 찾을 수 없습니다: " + studentId));
        return new CustomUserDetails(memberEntity);
    }

}
