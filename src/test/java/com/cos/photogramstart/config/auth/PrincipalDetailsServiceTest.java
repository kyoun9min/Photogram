package com.cos.photogramstart.config.auth;

import com.cos.photogramstart.domain.user.User;
import com.cos.photogramstart.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PrincipalDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PrincipalDetailsService principalDetailsService;

    @Test
    @DisplayName("아이디가 존재하면 PrincipalDetails를 정상적으로 반환한다")
    void loadUserByUsername_성공() {
        // given
        String username = "ssar";
        User user = User.builder()
                .username(username)
                .password("1234")
                .role("ROLE_USER")
                .build();

        given(userRepository.findByUsername(username)).willReturn(user);

        // when
        UserDetails userDetails = principalDetailsService.loadUserByUsername(username);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails instanceof PrincipalDetails).isTrue(); // 우리가 만든 타입인지 확인
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("존재하지 않는 아이디로 로그인 시 null을 반환한다")
    void loadUserByUsername_실패() {
        // given
        String username = "unknown";
        given(userRepository.findByUsername(username)).willReturn(null);

        // when
        UserDetails userDetails = principalDetailsService.loadUserByUsername(username);

        // then
        assertThat(userDetails).isNull();
        verify(userRepository, times(1)).findByUsername(username);
    }
}