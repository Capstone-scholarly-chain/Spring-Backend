package Baeksa.money.global.excepction;

import org.springframework.http.HttpStatus;

public enum ErrorCode implements BaseErrorCode {

    /// 공통
    INTERNAL_SERVER(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 오류가 발생했습니다."),

    /// 회원가입
    DUPICATED_MEMBER(HttpStatus.BAD_REQUEST, "MEMBER_001", "이미 가입된 학생입니다."),
    STUDENT_NOTFOUND(HttpStatus.NOT_FOUND, "MEMBER_002", "해당 학생 정보를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "MEMBER_003", "비밀번호가 일치하지 않습니다."),

    /// jwt
    EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "JWT_001", "refreshToken이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "JWT_002", "refreshToken이 유효하지 않습니다."),
    TOKEN_NOTFOUND(HttpStatus.NOT_FOUND, "JWT_003", "refreshToken이 없습니다."),
    INVALID_ID(HttpStatus.BAD_REQUEST, "JWT_004", "학번이 잘못되었습니다."),
    BLACKLISTED(HttpStatus.BAD_REQUEST, "JWT_005", "blacklist처리된 access 토큰"),
    INVALID_ACCESS(HttpStatus.BAD_REQUEST, "JWT_006", "accessToken이 유효하지 않습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
