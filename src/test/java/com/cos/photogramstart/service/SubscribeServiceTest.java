package com.cos.photogramstart.service;

import com.cos.photogramstart.domain.subscribe.SubscribeRepository;
import com.cos.photogramstart.handler.ex.CustomApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscribeServiceTest {

    @Mock
    private SubscribeRepository subscribeRepository;

    @InjectMocks
    private SubscribeService subscribeService;

    @Test
    @DisplayName("구독하기 성공 테스트")
    void 구독하기_성공() {
        // given
        int fromUserId = 1;
        int toUserId = 2;
        // mSubscribe는 void이므로 따로 given(doNothing)을 설정하지 않아도 기본적으로 성공 처리됨

        // when
        subscribeService.구독하기(fromUserId, toUserId);

        // then
        // mSubscribe가 실제로 1번 호출되었는지 검증
        verify(subscribeRepository, times(1)).mSubscribe(fromUserId, toUserId);
    }

    @Test
    @DisplayName("이미 구독 중일 때 에러 발생 테스트")
    void 구독하기_중복_실패() {
        // given
        int fromUserId = 1;
        int toUserId = 2;

        // mSubscribe 호출 시 예외가 발생한다고 가정
        doThrow(new RuntimeException())
                .when(subscribeRepository).mSubscribe(fromUserId, toUserId);

        // when & then
        assertThatThrownBy(() -> {
            subscribeService.구독하기(fromUserId, toUserId);
        }).isInstanceOf(CustomApiException.class)
                .hasMessageContaining("이미 구독을 하였습니다.");
    }

}