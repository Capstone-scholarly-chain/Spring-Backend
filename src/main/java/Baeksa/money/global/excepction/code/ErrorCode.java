package Baeksa.money.global.excepction.code;

import org.springframework.http.HttpStatus;

public enum ErrorCode implements BaseErrorCode {

    /// 공통
    INTERNAL_SERVER(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 오류가 발생했습니다."),

    /// AUTH
    DUPICATED_MEMBER(HttpStatus.BAD_REQUEST, "MEMBER_001", "이미 가입된 학생입니다."),
    STUDENT_NOTFOUND(HttpStatus.NOT_FOUND, "MEMBER_002", "해당 학생 정보를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "MEMBER_003", "비밀번호가 일치하지 않습니다."),
    INVALID_AUTH_HEADER(HttpStatus.UNAUTHORIZED, "MEMBER_004", "인증 헤더가 필요합니다."),
    INVALID_AUTH_HEADER2(HttpStatus.UNAUTHORIZED, "MEMBER_005", "잘못된 인증 형식입니다."),
    LOGOUT(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER_006", "로그아웃 실패"),

    /// jwt
    EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "JWT_001", "refreshToken이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "JWT_002", "refreshToken이 유효하지 않습니다."),
    TOKEN_NOTFOUND(HttpStatus.NOT_FOUND, "JWT_003", "refreshToken이 없습니다."),
    INVALID_ID(HttpStatus.BAD_REQUEST, "JWT_004", "학번이 잘못되었습니다."),
    BLACKLISTED(HttpStatus.FORBIDDEN, "JWT_005", "blacklist처리된 access 토큰"),
    INVALID_ACCESS(HttpStatus.BAD_REQUEST, "JWT_006", "accessToken이 유효하지 않습니다."),

    // pubsub/request/approve
    ALREADY_APPROVED(HttpStatus.CONFLICT, "PUBSUB_001", "이미 승인된 학생입니다."),
    ALREADY_REJECTED(HttpStatus.CONFLICT, "PUBSUB_002", "이미 거절된 학생입니다."),
    ALREADY_STATUS(HttpStatus.CONFLICT, "PUBSUB_003", "이미 등록된 학생입니다."),
    NOTSET_STATUS(HttpStatus.UNAUTHORIZED, "PUBSUB_004", "미승인 학생입니다."),
    NO_DEPOSIT(HttpStatus.NOT_FOUND, "PUBSUB_005", "입금 요청을 찾을 수 없습니다."),
    NO_WITHDRAW(HttpStatus.NOT_FOUND, "PUBSUB_006", "출금 요청을 찾을 수 없습니다."),
    INVALID_APPROVAL(HttpStatus.BAD_REQUEST, "PUBSUB_007", "승인할 수 없는 상태입니다."),
    NO_CHANNEL(HttpStatus.NOT_FOUND, "PUBSUB_008", "등록되지 않은 채널입니다."),

    //SUB 조회
    JSON_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_001", "JSON 직렬화 실패"),
    PENDING_DEPOSIT_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_002", "대기중인 입금 항목 조회 실패"),
    PENDING_WITHDRAW_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_003", "대기중인 출금 항목 조회 실패"),
    MY_HISTORY_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_004", "나의 내역 조회 실패"),

    DEPOSIT_CACHE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_005", "입금 항목 캐싱 실패"),
    WITHDRAW_CACHE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_006", "출금 항목 캐싱 실패"),

    VOTE_REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_007", "투표 등록 실패"),
    ONE_THEME_BALANCE_REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_008", "하나의 테마 잔액 등록 실패"),
    ALL_THEME_BALANCE_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_009", "테마 잔액 등록 실패"),

    DEPOSIT_CACHE_COMPLETE(HttpStatus.OK, "SUB_010", "입금 항목 캐싱 완료"),
    WITHDRAW_CACHE_COMPLETE(HttpStatus.OK, "SUB_011", "출금 항목 캐싱 완료"),

    VOTE_STATUS_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_012", "투표 현황 조회 실패"),
    ONE_THEME_BALANCE_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_013", "하나의 테마 잔액 조회 실패"),
    TOTAL_BALANCE_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUB_014", "전체 잔액 조회 실패"),

    //학생 가입 신청, 학생 입금 기입 요청, 학생 출금 승인, 학생 출금 거부,
    STUDENT_APPLY(HttpStatus.INTERNAL_SERVER_ERROR, "STUDENT_001", "학생 가입 신청 실패."),
    STUDENT_APPLY_LEDGER(HttpStatus.INTERNAL_SERVER_ERROR, "STUDENT_002", "학생 가입 신청 실패."),
    STUDENT_WITHDRAW(HttpStatus.INTERNAL_SERVER_ERROR, "STUDENT_003", "학생 출금 투표 실패"),


    //학생회 가입 승인, 학생회 가입 거절, 학생회 출금 기입 신청, 학생회 입금 승인, 학생회 입금 거절
    COMMITTEE_APPROVE(HttpStatus.INTERNAL_SERVER_ERROR, "COMMITTEE_001", "학생회 가입 승인 실패."),
    COMMITTEE_REJECT(HttpStatus.INTERNAL_SERVER_ERROR, "COMMITTEE_002", "학생회 가입 거절 실패."),
    COMMITTEE_WITHDRAW(HttpStatus.INTERNAL_SERVER_ERROR, "COMMITTEE_003", "학생회 출금 기입 신청 실패."),
    COMMITTEE_APPROVE_DEPOSIT(HttpStatus.INTERNAL_SERVER_ERROR, "COMMITTEE_004", "학생회 입금 승인 실패."),
    COMMITTEE_REJECT_DEPOSIT(HttpStatus.INTERNAL_SERVER_ERROR, "COMMITTEE_005", "학생회 입금 거절 실패."),

    //핑퐁 구조
    GET_STUDENTS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TIMEOUT_001", "학생 조직원 수 조회 실패"),
    STUDENT_COUNT_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "TIMEOUT_002", "학생 조직원 수 응답 타임아웃"),
    STUDENT_COUNT_NOT_AVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "TIMEOUT_003", "학생 조직원 수 응답 후에도 값이 없음"),
    REQUEST_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "TIMEOUT_003", "REQUEST_TIMEOUT"),
    REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TIMEOUT_003", "REQUEST_FAILED"),
    REQUEST_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "TIMEOUT_003", "REQUEST_INTERRUPTED"),
    DATA_NOT_AVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "TIMEOUT_003", "DATA_NOT_AVAILABLE"),
    GET_USER_STATUS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TIMEOUT_003", "GET_USER_STATUS_FAILED"),
    GET_PENDING_REQUESTS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TIMEOUT_003", "GET_PENDING_REQUESTS_FAILED"),
//    REQUEST_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "TIMEOUT_003", "학생 조직원 수 응답 후에도 값이 없음"),
/// /집가서 에러 수ㅜ정하기


    //sub - blockchain
    REGISTER_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_001", "회원가입 요청 처리 실패"),
    MEMBERSHIP_APPROVE_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_002", "회원 승인 요청 처리 실패"),
    MEMBERSHIP_REJECT_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_003", "회원 거절 요청 처리 실패"),
    MEMBERSHIP_PENDING_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_004", "대기중인 회원 요청 처리 실패"),
    MEMBERSHIP_STATUS_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_005", "회원 상태 조회 실패"),
    STUDENT_COUNT_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_006", "학생 수 조회 실패"),
    COUNCIL_COUNT_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_007", "학생회 수 조회 실패"),

    ADD_DEPOSIT_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_008", "입금 항목 추가 실패"),
    ADD_WITHDRAW_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_009", "출금 항목 추가 실패"),
    DEPOSIT_APPROVE_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_010", "입금 항목 승인 실패"),
    DEPOSIT_REJECT_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_011", "입금 항목 거절 실패"),
    WITHDRAW_VOTE_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_012", "출금 투표 요청 실패"),

    PENDING_DEPOSITS_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_013", "대기중인 입금 항목 조회 실패"),
    PENDING_WITHDRAWS_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_014", "대기중인 출금 항목 조회 실패"),
    WITHDRAW_STATUS_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_015", "출금 상태 조회 실패"),

    ONE_AMOUNTS_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_016", "하나의 테마 잔액 조회 실패"),
    AMOUNTS_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_017", "전체 잔액 조회 실패"),

    ONE_THEME_ITEMS_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_018", "하나의 테마 항목 조회 실패"),
    ALL_THEME_ITEMS_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "BLOCKCHAIN_019", "모든 테마 항목 조회 실패"),


    //fcm
    FCM_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "FCM_001", "FCM 알림 전송 실패"),
    FCMTOKEN_NOTFOUND(HttpStatus.NOT_FOUND, "FCM_002", "fcmToken이 없습니다."),



    // redis streams
    STREAMS_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "STREAMS_001", "스트림 전송 실패.");




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
