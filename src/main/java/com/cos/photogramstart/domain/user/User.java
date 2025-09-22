package com.cos.photogramstart.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String name;
    private String website; // 웹 사이트
    private String bio; // 자기 소개
    private String email;
    private String phone;
    private String gender;
    private String profileImageUrl; // 사진
    private String role; // 권한
    private LocalDateTime createDate;

    @PrePersist // 디비에 INSERT 되기 직전에 실행
    public void createDate() {
        this.createDate = LocalDateTime.now();
    }
}
