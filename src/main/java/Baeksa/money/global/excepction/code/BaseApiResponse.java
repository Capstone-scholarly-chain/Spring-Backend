package Baeksa.money.global.excepction.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseApiResponse<T> {
    private int status;
    private String code;
    private String message;
    private T data;
}