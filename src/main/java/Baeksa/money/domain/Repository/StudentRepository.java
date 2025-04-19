package Baeksa.money.domain.Repository;

import Baeksa.money.domain.Entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {
    Optional<StudentEntity> findByStudentId(Long studentId);
//    Optional<StudentEntity> findByName(String name);
//    Optional<StudentEntity> findByPhoneNumber(String phoneNumber);
}
