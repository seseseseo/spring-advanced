package org.example.expert.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class PersistenceConfig {
    //엔티티의 생성일자, 수정일자를 자동으로 관리할 수 있게 설정
    // 왜하냐?
}
