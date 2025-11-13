package com.cos.photogramstart.web.dto.auth;

import com.cos.photogramstart.domain.user.User;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupDto {

    @Max(20)
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String email;
    @NotBlank
    private String name;

    public User toEntity() {
        return User.builder()
                .username(username)
                .password(password)
                .email(email)
                .name(name)
                .build();
    }

}
