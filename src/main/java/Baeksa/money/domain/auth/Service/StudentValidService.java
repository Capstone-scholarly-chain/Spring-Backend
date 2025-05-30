package Baeksa.money.domain.auth.Service;

import Baeksa.money.domain.auth.Entity.StudentEntity;
import Baeksa.money.domain.auth.Repository.StudentRepository;
import Baeksa.money.domain.auth.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentValidService {


    private final StudentRepository studentRepository;
    

    //학번으로 학생 정보 조회는 jpa가 할거고, 검증
    public boolean signupValid(String studentId, String username, String phoneNumber, Role role) {
        Optional<StudentEntity> studentOptional = studentRepository.findByStudentId(studentId);
        //타입은 엔티티로

        if(studentOptional.isPresent()) {
            //학생이 존재하면 get으로 값을 꺼내고 여기 게터와 파라미터를 equals로 비교
            StudentEntity student = studentOptional.get();

            log.info("[DB] studentId: {}", student.getStudentId());
            log.info("[DB] username: {}", student.getUsername());

            return student.getStudentId().equals(studentId) && student.getUsername().equals(username) &&
                    student.getPhoneNumber().equals(phoneNumber) && student.getRole().equals(role);
        }
        return false;
    }

}
