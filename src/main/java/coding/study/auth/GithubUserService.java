package coding.study.auth;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

// GitHub 로그인이 끝나면 GitHub 사용자 정보로 users를 만들거나 갱신하고, 세션에 둘 principal을 만들어요.
@Service
public class GithubUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    static final String USER_ID = "userId";

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;
    private final Clock clock;

    public GithubUserService(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        return toPrincipal(delegate.loadUser(request).getAttributes());
    }

    // principal 이름을 우리 user id로 해요. 그러면 Spring Session의 PRINCIPAL_NAME이 user id가 돼서,
    // 탈퇴할 때 그 사용자의 세션을 모두 지울 수 있어요 (ADR-002).
    // GitHub 응답 전체를 세션에 저장하지 않도록 필요한 값만 남겨요.
    OAuth2User toPrincipal(Map<String, Object> github) {
        long githubId = ((Number) github.get("id")).longValue();
        long userId = userRepository.upsert(
                githubId, (String) github.get("login"), (String) github.get("avatar_url"), clock.instant());
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(USER_ID, String.valueOf(userId)),
                USER_ID);
    }

}
