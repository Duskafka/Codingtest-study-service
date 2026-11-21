package coding.study.auth;

import static org.assertj.core.api.Assertions.assertThat;

import coding.study.TestcontainersConfiguration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

// 쿠키는 MockMvc가 실제와 다르게 만들어서 (세션 쿠키 속성, XSRF-TOKEN 누락), 진짜 Tomcat을 띄워 응답 헤더로 확인해요.
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CookieTest {

    @LocalServerPort
    int port;

    @Test
    void GET만_해도_SPA가_읽을_수_있는_XSRF_TOKEN_쿠키가_내려온다() throws Exception {
        String xsrf = cookie(setCookies("/actuator/health"), "XSRF-TOKEN");

        assertThat(xsrf).doesNotContain("HttpOnly");
    }

    @Test
    void 세션_쿠키는_HttpOnly_Secure_SameSite_Lax_30일이다() throws Exception {
        // 로그인 시작 요청은 OAuth state를 세션에 저장해서 세션 쿠키가 생겨요.
        String session = cookie(setCookies("/oauth2/authorization/github"), "SESSION");

        assertThat(session).contains("HttpOnly", "Secure", "SameSite=Lax", "Max-Age=2592000");
    }

    private List<String> setCookies(String path) throws Exception {
        HttpResponse<Void> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build(),
                HttpResponse.BodyHandlers.discarding());
        return response.headers().allValues("Set-Cookie");
    }

    private static String cookie(List<String> setCookies, String name) {
        return setCookies.stream().filter(c -> c.startsWith(name + "=")).findFirst().orElseThrow();
    }

}
