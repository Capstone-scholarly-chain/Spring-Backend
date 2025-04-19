package Baeksa.money.domain.Repository;

import Baeksa.money.domain.Entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    //<엔티티 클래스, 기본키>
    boolean existsByStudentId(Long studentId);

    //jwt CustomUserDetailsService
    Optional<MemberEntity> findByStudentId(Long studentId);

}
