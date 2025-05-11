package Baeksa.money.global.config.swagger;

import Baeksa.money.global.excepction.code.BaseErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExample {
//    Class<? extends BaseErrorCode> value();

    Class<? extends BaseErrorCode> value();    // 필수: enum 클래스
    String[] include() default {};
}

