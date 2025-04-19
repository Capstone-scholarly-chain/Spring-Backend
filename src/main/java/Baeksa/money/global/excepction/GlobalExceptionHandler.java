package Baeksa.money.global.excepction;


import Baeksa.money.domain.Dto.MemberDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //CustomException 예외가 터졌을때 이 메서드가 실행
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<MemberDto.ErrorResponse> handlerCustomException(CustomException ex){
        MemberDto.ErrorResponse response = MemberDto.ErrorResponse.of(ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

}
