package Baeksa.money.global.jwt;

import Baeksa.money.domain.auth.Entity.MemberEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final MemberEntity memberEntity;

    public CustomUserDetails(MemberEntity memberEntity) {
        this.memberEntity = memberEntity;
    }

    // 권한 설정
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(memberEntity.getRole().name()));
    }

    @Override
    public String getPassword() {
        return memberEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return memberEntity.getStudentId();
    }

    public String getRealUsername() {
        return memberEntity.getUsername();
    }

    public String getStudentId() {
        return memberEntity.getStudentId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
