package com.cos.photogramstart.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSearchDto {

    private int id;

    private String username;

    private String name;

    private String s3ProfileImageUrl;
}
