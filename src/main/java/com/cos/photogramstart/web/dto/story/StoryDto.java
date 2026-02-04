package com.cos.photogramstart.web.dto.story;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private String caption;

    private String s3PostImageUrl;

    private int likeCount;

    private boolean likeState;

    private String username;

    private String s3ProfileImageUrl;

    private List<CommentDto> comments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDto implements Serializable {

        private static final long serialVersionUID = 1L;

        private int id;

        private String content;

        private String commentUsername;

        private int commentUserId;     // 내 댓글 삭제 버튼 판별용
    }
}
