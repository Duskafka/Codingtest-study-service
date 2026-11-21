-- 사용자 (계획 4.3). 탈퇴하면 github_id를 NULL로, github_login을 '탈퇴한 사용자'로 익명화해요 (5.16).
-- PostgreSQL의 UNIQUE는 NULL을 여러 개 허용해서, 익명화된 행이 여러 개여도 돼요.
CREATE TABLE users (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    github_id    BIGINT UNIQUE,
    github_login VARCHAR(39) NOT NULL,
    avatar_url   TEXT,
    created_at   TIMESTAMPTZ NOT NULL,
    deleted_at   TIMESTAMPTZ
);
