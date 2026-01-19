package com.cos.photogramstart.service;

import com.cos.photogramstart.domain.comment.Comment;
import com.cos.photogramstart.domain.comment.CommentRepository;
import com.cos.photogramstart.domain.image.Image;
import com.cos.photogramstart.domain.user.User;
import com.cos.photogramstart.domain.user.UserRepository;
import com.cos.photogramstart.handler.ex.CustomApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    private final UserRepository userRepository;

    @Transactional
    public Comment 댓글쓰기(String content, int imageId, int userId) {

        // comment에는 imageId 값만 필요하므로, 굳이 findbyId로 db에서 객체를 조회해서 쓸 필요 없이 id만 가진 빈객체를 씀.
        Image image = new Image();
        image.setId(imageId);

        User userEntity = userRepository.findById(userId).orElseThrow(() -> new CustomApiException("유저 아이디를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(content)
                .image(image)
                .user(userEntity)
                .build();

        return commentRepository.save(comment);
    }

    @Transactional
    public void 댓글삭제() {

    }
}
