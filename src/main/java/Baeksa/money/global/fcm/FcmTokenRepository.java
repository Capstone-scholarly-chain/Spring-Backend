package Baeksa.money.global.fcm;

import Baeksa.money.global.redis.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends CrudRepository<FcmToken, Long> {
    Optional<FcmToken> findById(Long studentId);
//    List<FcmToken> findAllById(Long studentId);
}

