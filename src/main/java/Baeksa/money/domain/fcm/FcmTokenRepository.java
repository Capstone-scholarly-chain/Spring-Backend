package Baeksa.money.domain.fcm;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FcmTokenRepository extends CrudRepository<FcmToken, String> {
    Optional<FcmToken> findById(String studentId);
//    List<FcmToken> findAllById(Long studentId);
}

