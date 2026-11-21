package coding.study.auth;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 저장은 UserRepository.upsert로만 해요. 그래서 id 생성 전략을 두지 않아요.
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    private Long id;

    private Long githubId;

    private String githubLogin;

    private String avatarUrl;

    private Instant createdAt;

    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

}
