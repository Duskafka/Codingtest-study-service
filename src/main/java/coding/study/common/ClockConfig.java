package coding.study.common;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 현재 시각은 모두 이 Clock에서 가져와요. 테스트에서 고정 Clock으로 바꿀 수 있어요 (계획 6장 ②).
@Configuration(proxyBeanMethods = false)
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

}
