package com.cos.photogramstart.service;

import com.cos.photogramstart.domain.subscribe.Subscribe;
import com.cos.photogramstart.domain.subscribe.SubscribeRepository;
import com.cos.photogramstart.handler.ex.CustomApiException;
import com.cos.photogramstart.s3.component.S3UrlResolver;
import com.cos.photogramstart.s3.config.AwsS3Config;
import com.cos.photogramstart.web.dto.subscribe.SubscribeDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;

    private final EntityManager em;

    private final S3UrlResolver s3UrlResolver;

    @Transactional(readOnly = true)
    public List<SubscribeDto> 구독리스트(int principalId, int pageUserId) {

        StringBuffer sb = new StringBuffer();
        sb.append("SELECT u.id, u.username, u.profileImageUrl, ");
        sb.append("if ((SELECT 1 FROM subscribe_tb WHERE fromUserId = ? AND toUserId = u.id), 1, 0) subscribeState, ");
        sb.append("if ((? = u.id), 1, 0) equalUserState ");
        sb.append("FROM user_tb u INNER JOIN subscribe_tb s ");
        sb.append("ON u.id = s.toUserId ");
        sb.append("WHERE s.fromUserId = ?"); // 세미콜론 첨부하면 안됨

        // 1. 물음표 principalId
        // 2. 물음표 principalId
        // 3. 물음표 pageUserId

        Query query = em.createNativeQuery(sb.toString())
                .setParameter(1, principalId)
                .setParameter(2, principalId)
                .setParameter(3, pageUserId);

        // 쿼리 실행 (qlrm 라이브러리 필요 = Dto에 DB결과를 매핑하기 위해서)
        JpaResultMapper result = new JpaResultMapper();
        List<SubscribeDto> subscribeDtos = result.list(query, SubscribeDto.class);

        subscribeDtos.forEach(subscribeDto -> {
            subscribeDto.setProfileImageUrl(s3UrlResolver.resolve(subscribeDto.getProfileImageUrl()));
        });

        return subscribeDtos;
    }

    @Transactional
    public void 구독하기(int fromUserId, int toUserId) {

        try {
            subscribeRepository.mSubscribe(fromUserId, toUserId);
        } catch (Exception e) {
            throw new CustomApiException("이미 구독을 하였습니다.");
        }
    }

    @Transactional
    public void 구독취소하기(int fromUserId, int toUserId) {

        subscribeRepository.mUnSubscribe(fromUserId, toUserId);
    }
}
