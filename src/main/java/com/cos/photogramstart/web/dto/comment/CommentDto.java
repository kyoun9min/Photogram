package com.cos.photogramstart.web.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CommentDto {

    @NotBlank // 빈값, 공백(space)문자열, null 체크 -> 내부에서 trim() > 0 으로 공백 다 지운 문자열 길이가 0보다 큰지 체크 + null 체크
    private String content;

    @NotNull
    @Positive
    private Integer imageId;
}
