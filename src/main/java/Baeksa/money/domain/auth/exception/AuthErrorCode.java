package Baeksa.money.domain.auth.exception;

import Baeksa.money.global.excepction.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum AuthErrorCode implements BaseErrorCode {

    //enum에서는 반드시 상수 먼저, 필드 나중
    /// 회원가입
    DUPICATED_MEMBER(HttpStatus.BAD_REQUEST, "MEMBER_001", "이미 가입된 학생입니다."),
    STUDENT_NOTFOUND(HttpStatus.NOT_FOUND, "MEMBER_002", "해당 학생 정보를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "MEMBER_003", "비밀번호가 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
