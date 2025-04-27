package Baeksa.money.domain.Service;

import Baeksa.money.domain.Entity.StudentEntity;
import Baeksa.money.domain.Repository.StudentRepository;
import Baeksa.money.domain.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StudentService {


    private StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }


    //학번으로 학생 정보 조회는 jpa가 할거고, 검증
    public boolean signupValid(Long studentId, String name, String phoneNumber, Role role) {
        Optional<StudentEntity> studentOptional = studentRepository.findByStudentId(studentId);
        //타입은 엔티티로

        if(studentOptional.isPresent()) {
            //학생이 존재하면 get으로 값을 꺼내고 여기 게터와 파라미터를 equals로 비교
            StudentEntity student = studentOptional.get();

            System.out.println("✅ [DB] studentId: " + student.getStudentId());
            System.out.println("✅ [DB] username: " + student.getUsername());
            System.out.println("✅ [DB] phone: " + student.getPhoneNumber());
            System.out.println("✅ [DB] role: " + student.getRole());

            System.out.println("✅ [입력값] studentId: " + studentId);
            System.out.println("✅ [입력값] username: " + name);
            System.out.println("✅ [입력값] phone: " + phoneNumber);
            System.out.println("✅ [입력값] role: " + role);

            return student.getStudentId().equals(studentId) && student.getUsername().equals(name) &&
                    student.getPhoneNumber().equals(phoneNumber) && student.getRole().equals(role);
        }
        System.out.println("❌ studentOptional.isPresent() == false");
        return false;
    }

}
