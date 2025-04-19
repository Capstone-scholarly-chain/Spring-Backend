package Baeksa.money.global.redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
//entity, @Id의 타입
    Optional<RefreshToken> findById(Long studentId);
    void deleteById(Long studentId);
}
