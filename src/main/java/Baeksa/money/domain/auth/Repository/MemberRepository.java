package Baeksa.money.domain.auth.Repository;

import Baeksa.money.domain.auth.Entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    //<엔티티 클래스, 기본키>
    boolean existsByStudentId(String studentId);

    //jwt CustomUserDetailsService
    Optional<MemberEntity> findByStudentId(String studentId);

}
