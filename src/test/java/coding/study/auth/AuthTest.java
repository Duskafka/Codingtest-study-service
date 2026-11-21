package coding.study.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import coding.study.TestcontainersConfiguration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    GithubUserService githubUserService;

    @Autowired
    UserRepository userRepository;

    @Test
    void health는_로그인_없이_열린다() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void 로그인하지_않고_API를_부르면_리다이렉트가_아니라_401() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void GitHub_로그인은_같은_github_id면_같은_사용자를_갱신한다() {
        OAuth2User first = githubUserService.toPrincipal(github(1001L, "old-login"));
        OAuth2User second = githubUserService.toPrincipal(github(1001L, "new-login"));

        assertThat(second.getName()).isEqualTo(first.getName());
        User user = userRepository.findById(Long.valueOf(first.getName())).orElseThrow();
        assertThat(user.getGithubId()).isEqualTo(1001L);
        assertThat(user.getGithubLogin()).isEqualTo("new-login");
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void 로그인한_사용자는_내_정보를_받는다() throws Exception {
        OAuth2User principal = githubUserService.toPrincipal(github(1002L, "me"));

        mvc.perform(get("/api/me").with(oauth2Login().oauth2User(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Long.valueOf(principal.getName())))
                .andExpect(jsonPath("$.githubLogin").value("me"))
                .andExpect(jsonPath("$.avatarUrl").value("https://avatars.example/1002"));
    }

    @Test
    void 로그아웃은_CSRF_토큰이_있어야_한다() throws Exception {
        OAuth2User principal = githubUserService.toPrincipal(github(1003L, "logout"));

        mvc.perform(post("/logout").with(oauth2Login().oauth2User(principal)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/logout").with(oauth2Login().oauth2User(principal)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    // GitHub GET /user 응답 중 쓰는 필드만 (email 같은 나머지는 무시해요)
    private static Map<String, Object> github(long id, String login) {
        return Map.of("id", id, "login", login, "avatar_url", "https://avatars.example/" + id, "email", "x@example.com");
    }

}
