package Baeksa.money.global.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // 또는 "http://localhost:3000"
                .allowedMethods("*")
                .allowCredentials(true)
                .allowedHeaders("*")
                .exposedHeaders("Authorization");
    }
}
