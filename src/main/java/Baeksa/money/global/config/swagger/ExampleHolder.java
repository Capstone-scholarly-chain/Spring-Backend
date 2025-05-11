package Baeksa.money.global.config.swagger;

import io.swagger.v3.oas.models.examples.Example;
import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class ExampleHolder {
    // 스웨거의 Example 객체입니다. 위 스웨거 분석의 Example Object 참고.
    private Example holder;
    private String name;
    private int status;
}