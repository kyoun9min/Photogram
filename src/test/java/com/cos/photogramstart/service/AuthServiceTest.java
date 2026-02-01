package com.cos.photogramstart.service;

import com.cos.photogramstart.domain.user.User;
import com.cos.photogramstart.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder; // BCryptPasswordEncoder

    @InjectMocks
    private AuthService authService;

    // bCryptPasswordEncoder가 암호화를 잘 했느냐를 검증하는게 아닌 UserEntity에 암호화된 패스워드가 잘 저장되었는지를 확인한다.
    @Test
    @DisplayName("회원가입 시 비밀번호가 암호화되어 저장되어야 한다")
    void 회원가입_비밀번호_암호화_테스트() {
        // given
        User user = User.builder()
                .username("ssar")
                .password("1234") // 원래 비밀번호
                .build();

        String encodedPassword = "ENC_1234"; // 암호화된 결과라고 가정

        // 1. passwordEncoder.encode("1234")가 호출되면 "ENC_1234"를 반환하라고 설정
        given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);

        // 2. repository.save()가 호출되면 저장된 유저 엔티티를 반환하라고 설정
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        // invocation -> invocation.getArgument(0)
        // 의미: save 메서드에 인자로 전달된 '가공된 User 객체'를 그대로 리턴값으로 사용하겠다.
        // 이유: 서비스 로직에서 변경된 필드(암호화된 비번, ROLE 등)를 테스트 코드에서 검증하기 위함.

        // when
        User userEntity = authService.회원가입(user);

        // then
        // 1. 진짜로 암호화된 비밀번호가 세팅되었는지 검증
        assertThat(userEntity.getPassword()).isEqualTo(encodedPassword);

        // 2. 권한이 ROLE_USER로 잘 들어갔는지 검증
        assertThat(userEntity.getRole()).isEqualTo("ROLE_USER");

        // 3. encode 메서드와 save 메서드가 각각 한 번씩 실행되었는지 확인
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }
}