package coding.study.auth;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

// 로그인과 권한 (계획 4.5). 세션은 Spring Session JDBC로 DB에 저장돼요 (ADR-002).
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, GithubUserService githubUserService) {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                // API는 로그인 페이지로 보내지 않고 401을 줘요. SPA가 401을 보고 로그인 버튼을 보여 줘요.
                .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), pathPattern("/api/**")))
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(info -> info.userService(githubUserService))
                        .defaultSuccessUrl("/", true))
                // XSRF-TOKEN 쿠키를 내려주고, SPA가 X-XSRF-TOKEN 헤더로 돌려보내요 (CookieTest에서 확인).
                .csrf(csrf -> csrf.spa())
                .logout(logout -> logout.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                .build();
    }

}
