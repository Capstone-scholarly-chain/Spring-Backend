package Baeksa.money.global.config;

import Baeksa.money.global.jwt.CustomUserDetailsService;
import Baeksa.money.global.jwt.JWTFilter;
import Baeksa.money.global.jwt.JWTUtil;
//import Baeksa.money.global.jwt.LoginFilter;
import Baeksa.money.global.redis.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.logging.Filter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    private String[] allowUrl = {
            "/login",
            "/signup",
            "/api/auth/reissue",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/pubsub/**",
            "/signup-test",
            "/streams/**",
            "api/fcm/**",
            "api/ledger/**",
            "api/committee/**",
            "api/student/**",
            "api/s3/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf((auth) -> auth.disable());

        http
                .formLogin((auth) -> auth.disable());

        http
                .httpBasic(Customizer.withDefaults());
        http
                .csrf(AbstractHttpConfigurer::disable)
                //세션 없이 stateless하게 설정
                .sessionManagement((sessionManagement) ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                //인증이 필요한 url 설정
                .authorizeHttpRequests((authorizeRequests) ->
                        authorizeRequests
                                //아래 url은 인증 필요하지 않음
                                .requestMatchers(allowUrl).permitAll()
                                .requestMatchers("/api/pubsub/student/**").hasAnyRole("STUDENT", "ADMIN")
                                .requestMatchers("/api/pubsub/committee/**").hasAnyRole("COMMITTEE", "ADMIN")
                                .requestMatchers("/api/**").authenticated()
                );

        http
//                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshTokenService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter(jwtUtil, customUserDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JWTFilter jwtFilter(JWTUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        return new JWTFilter(jwtUtil, customUserDetailsService);
    }

}
