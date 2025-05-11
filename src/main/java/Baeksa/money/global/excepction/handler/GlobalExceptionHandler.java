package Baeksa.money.global.excepction.handler;


import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //CustomException 예외가 터졌을때 이 메서드가 실행
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handlerCustomException(CustomException ex){
        ErrorResponse response = ErrorResponse.of(ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

}
