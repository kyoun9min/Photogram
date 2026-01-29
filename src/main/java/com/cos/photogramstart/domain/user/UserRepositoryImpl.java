package com.cos.photogramstart.domain.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import java.util.List;
import static com.cos.photogramstart.domain.user.QUser.user;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> findByNameContaining(String name) {
        return queryFactory
                .selectFrom(user)
                .where(user.name.contains(name)) // 'like %name%' 효과
                .fetch();
    }
}
