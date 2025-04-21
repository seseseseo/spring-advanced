package org.example.expert.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {

    // 입력받은 비밀번호를 저 방식으로 암호화
    public String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(BCrypt.MIN_COST, rawPassword.toCharArray());
    }

    // 입력 비밀번호랑 저장된 암호화 비밀번호가 일치하는지 검증
    public boolean matches(String rawPassword, String encodedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword);
        return result.verified;
    }
}
