package Baeksa.money.global.config.swagger;

import Baeksa.money.global.excepction.code.BaseErrorCode;
import Baeksa.money.global.excepction.code.ErrorResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class SwaggerConfig {

    private ApplicationContext applicationContext;

    @Bean
    public OpenAPI swagger() {
        Info info = new Info()
                .title("블록체인을 활용한 학생회 장부 시스템")
                .description("실습용 Swagger")
                .version("0.0.1");

        Components components = new Components()
                // ✅ Bearer 인증
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                // ✅ Basic 인증 추가
                .addSecuritySchemes("basicAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic"));

        // 기본으로 Bearer 적용 (원하면 생략 가능)
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .addServersItem(new Server().url("/"))
                .components(components)
                .addSecurityItem(securityRequirement); // 기본 인증은 Bearer로
    }
//        Info info = new Info().title("블록체인을 활용한 학생회 장부 시스템").description("실습용 Swagger").version("0.0.1");
//
//        String securityScheme = "bearerAuth";
//        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityScheme);
//
//        Components components = new Components()
//                .addSecuritySchemes(securityScheme, new SecurityScheme()
//                        .name(securityScheme)
//                        .type(SecurityScheme.Type.HTTP)
//                        .scheme("Bearer")
//                        .bearerFormat("JWT"));
//
//        return new OpenAPI()
//                .info(info)
//                .addServersItem(new Server().url("/"))
//                .addSecurityItem(securityRequirement)
//                .components(components);
//    }

    /**
     * BaseErrorCode 타입의 이넘값들을 문서화 시킵니다. ExplainError 어노테이션으로 부가설명을 붙일수있습니다. 필드들을 가져와서 예시 에러 객체를
     * 동적으로 생성해서 예시값으로 붙입니다.
     */
    private void generateErrorCodeResponseExample(Operation operation, Class<? extends BaseErrorCode> type) {
        ApiResponses responses = operation.getResponses();

        BaseErrorCode[] errorCodes = type.getEnumConstants();

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders =
                Arrays.stream(errorCodes)
                        .map(errorCode -> ExampleHolder.builder()
                                .holder(getSwaggerExample(errorCode.getMessage(), errorCode))
                                .status(errorCode.getHttpStatus().value())
                                .name(errorCode.getCode())
                                .build())
                        .collect(Collectors.groupingBy(ExampleHolder::getStatus));

        addExamplesToResponses(responses, statusWithExampleHolders);
    }

//에러를 다양하게 분리했으면 이걸 해줘야 하는데 난 도메인 구분없이 ErrorCode에 다 넣어버렸음
//    private void generateExceptionResponseExample(Operation operation, Class<?> type) {
//        ApiResponses responses = operation.getResponses();

    /**
     * : 주어진 ErrorReason을 사용하여 ErrorResponse 객체를 생성합니다. 이 객체는 예시 응답에 포함됩니다.
     */
    private Example getSwaggerExample(String description, BaseErrorCode errorCode) {
        ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        Example example = new Example();
        example.description(description);
        example.setValue(errorResponse);
        return example;
    }

    /**
     * 상태 코드별로 예시를 API 응답에 추가합니다.
     */
    private void addExamplesToResponses(
            ApiResponses responses, Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach((status, holders) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            ApiResponse apiResponse = new ApiResponse();
            holders.forEach(holder ->
                    mediaType.addExamples(holder.getName(), holder.getHolder()));
            content.addMediaType("application/json", mediaType);
            apiResponse.setContent(content);
            responses.addApiResponse(String.valueOf(status), apiResponse);
        });
    }

    /**
     * API 메서드 및 클래스에 정의된 Tag 어노테이션을 가져와 태그 이름을 추출하고 리스트에 추가 합니다.
     */
    private static List<String> getTags(HandlerMethod handlerMethod) {
        List<String> tags = new ArrayList<>();

        Tag[] methodTags = handlerMethod.getMethod().getAnnotationsByType(Tag.class);
        List<String> methodTagStrings =
                Arrays.stream(methodTags).map(Tag::name).collect(Collectors.toList());

        Tag[] classTags = handlerMethod.getClass().getAnnotationsByType(Tag.class);
        List<String> classTagStrings =
                Arrays.stream(classTags).map(Tag::name).collect(Collectors.toList());
        tags.addAll(methodTagStrings);
        tags.addAll(classTagStrings);
        return tags;
    }

//    @Bean
//    public OperationCustomizer applyErrorCodes() {
//        return (operation, handlerMethod) -> {
//            ApiErrorCodeExample annotation = handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);
//            if (annotation != null) {
//                generateErrorCodeResponseExample(operation, annotation.value());
//            }
//            return operation;
//        };
//    }


    @Bean
    public OperationCustomizer applyErrorCodeExamples() {
        return (operation, handlerMethod) -> {
            ApiErrorCodeExample annotation = handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);
            if (annotation != null) {
                Class<? extends BaseErrorCode> enumType = annotation.value();
                Set<String> includes = Arrays.stream(annotation.include()).collect(Collectors.toSet());

                BaseErrorCode[] filtered = Arrays.stream(enumType.getEnumConstants())
                        .filter(code -> includes.isEmpty() || includes.contains(((Enum<?>) code).name()))
                        .toArray(BaseErrorCode[]::new);

                Map<Integer, List<ExampleHolder>> grouped =
                        Arrays.stream(filtered)
                                .map(code -> ExampleHolder.builder()
                                        .status(code.getHttpStatus().value())
                                        .name(code.getCode())
                                        .holder(getSwaggerExample(code.getMessage(), code))
                                        .build())
                                .collect(Collectors.groupingBy(ExampleHolder::getStatus));

                addExamplesToResponses(operation.getResponses(), grouped);
            }
            return operation;
        };
    }


}
