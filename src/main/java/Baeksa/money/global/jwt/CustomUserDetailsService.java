package Baeksa.money.global.jwt;

import Baeksa.money.domain.Entity.MemberEntity;
import Baeksa.money.domain.Repository.MemberRepository;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long studentId = Long.parseLong(username);
            MemberEntity memberEntity = memberRepository.findByStudentId(studentId)
                    .orElseThrow(() -> new UsernameNotFoundException("해당 학번의 사용자를 찾을 수 없습니다: " + studentId));
//위에 이거 진짜 많이 사용하니까 람다표현식 외우기
            return new CustomUserDetails(memberEntity);

        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("학번 형식이 올바르지 않습니다.");
        }
    }

}
