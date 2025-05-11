package Baeksa.money.domain.auth.Repository;

import Baeksa.money.domain.auth.Entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {
    Optional<StudentEntity> findByStudentId(String studentId);
//    Optional<StudentEntity> findByName(String name);
//    Optional<StudentEntity> findByPhoneNumber(String phoneNumber);
}
