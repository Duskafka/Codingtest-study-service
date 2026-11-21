package coding.study;

import org.springframework.boot.SpringApplication;

// 로컬 실행용: ./gradlew bootTestRun 으로 PostgreSQL 컨테이너와 함께 앱을 띄워요.
public class TestCodingtestStudyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(CodingtestStudyServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
