package org.example.expert.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        //이미 존재하는 이메일일 경우 예외 발생
        
        /*
          이부분 수정@!!!!!!
        * 발생 이메일 중복 체크를 가장 먼저 수행한 후 통과한 경우에만 비밀번호를 암호화하도록 위치를 바꿈
         */
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new InvalidRequestException("이미 존재하는 이메일입니다.");
        }

        //입력받은 비밀번호를 암호화하고
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        // 입력받은 사용자 역할을 Enum으로 반환하고
        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        // 새 User   객체 생성하고
        User newUser = new User(
                signupRequest.getEmail(),
                encodedPassword,
                userRole
        );
        // 레파지토리에 디비 저장
        User savedUser = userRepository.save(newUser);
        // 저장된 유저 정보로 JWT 토큰을 생성한다
        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), userRole);
        // 이 토큰을 열기에 넣어서 반환한다
        return new SignupResponse(bearerToken);
    }

    @Transactional(readOnly = true)
    public SigninResponse signin(SigninRequest signinRequest) {
        // 이메일 기반으로 디비에서 사용자를 조회하고
        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                () -> new InvalidRequestException("가입되지 않은 유저입니다."));

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new AuthException("잘못된 비밀번호입니다.");
        }

        //인증에 성공하면 JWT 토큰 생성
        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
        // 로그인 성공시 토근 발급 후 응답하는 구조
        return new SigninResponse(bearerToken);
    }
}
