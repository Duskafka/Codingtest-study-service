package coding.study.auth;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인할 때마다 호출해요. 첫 로그인이면 만들고, 아니면 GitHub에서 바뀐 login·프로필 사진을 반영해요.
    // 한 문장이라 첫 로그인 요청이 동시에 두 번 와도 행이 하나만 생겨요.
    // 쓰기 쿼리라 읽기 전용 트랜잭션이 되지 않도록 @Transactional을 직접 붙여요.
    @Transactional
    @Query(nativeQuery = true, value = """
            INSERT INTO users (github_id, github_login, avatar_url, created_at)
            VALUES (:githubId, :githubLogin, :avatarUrl, :now)
            ON CONFLICT (github_id) DO UPDATE
                SET github_login = EXCLUDED.github_login,
                    avatar_url   = EXCLUDED.avatar_url
            RETURNING id
            """)
    long upsert(long githubId, String githubLogin, String avatarUrl, Instant now);

}
