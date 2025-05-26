package Baeksa.money.domain.fcm.enums;

public enum NotiType {
    REGISTER_USER,		//학생 회원가입 및 조직 신청
    APPROVE_MEMBERSHIP,	//학생회가 학생 가입 승인
    REJECT_MEMBERSHIP,	//학생회가 학생 가입 거절
    STUDENT_APPLY_LEDGER,	//학생 입금 기입 요청
    COMMITTEE_APPROVE_DEPOSIT,	//학생회가 입금 내역 승인
    COMMITTEE_REJECT_DEPOSIT,	//학생회가 입금 내역 거절
    COMMITTEE_APPLY_WITHDRAW,	//학생회 출금 기입 요청
    STUDENT_VOTE_WITHDRAW	//학생이 출금 승인 투표
}
