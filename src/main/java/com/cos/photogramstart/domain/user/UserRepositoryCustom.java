package com.cos.photogramstart.domain.user;

import java.util.List;

public interface UserRepositoryCustom {

    List<User> findByNameContaining(String name);

}
