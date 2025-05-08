package Baeksa.money.domain.converter;

import Baeksa.money.domain.Dto.MemberDto;
import Baeksa.money.domain.Entity.MemberEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MemberConverter {

    private final BCryptPasswordEncoder passwordEncoder;

    public MemberConverter(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    //    public MemberEntity toEntity(){
//        MemberEntity memberEntity = new MemberEntity();
//        memberEntity.setName(this.name);
//        memberEntity.setEmail(this.email);
//        memberEntity.setStudentId(this.studentId);
//        memberEntity.setPassword(this.password);
//        memberEntity.setCommittee(this.isCommittee);
//    }

    // DTO -> Entity (회원가입용)
    public MemberEntity toEntity(MemberDto dto) {
        return MemberEntity.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .studentId(dto.getStudentId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();
    }

    // Entity -> DTO (조회용)
    //응답 확인용 dto
    public MemberDto.MemberResponseDto toResponseDto(MemberEntity entity) {
        return MemberDto.MemberResponseDto.builder()
                .username(entity.getUsername())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .studentId(entity.getStudentId())
                .role(entity.getRole())
                .status(entity.getStatus())
                .timestamp(entity.getTimestamp())
                .build();
    }

}

