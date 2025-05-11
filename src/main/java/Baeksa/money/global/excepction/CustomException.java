package Baeksa.money.global.excepction;

import Baeksa.money.global.excepction.code.BaseErrorCode;

public class CustomException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public CustomException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BaseErrorCode getErrorCode() {
        return errorCode; }

}
