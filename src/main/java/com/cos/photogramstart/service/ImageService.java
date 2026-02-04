package com.cos.photogramstart.service;

import com.cos.photogramstart.config.auth.PrincipalDetails;
import com.cos.photogramstart.domain.image.Image;
import com.cos.photogramstart.domain.image.ImageRepository;
import com.cos.photogramstart.handler.ex.CustomException;
import com.cos.photogramstart.s3.component.S3UrlResolver;
import com.cos.photogramstart.web.dto.image.ImageUploadDto;
import com.cos.photogramstart.web.dto.story.StoryDto;
import com.cos.photogramstart.web.dto.story.StoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ImageService {

    private final ImageRepository imageRepository;

    private final S3Client s3Client;

    private final S3UrlResolver s3UrlResolver;

    @Value("${s3.bucket}")
    private String bucketName;

    @Transactional(readOnly = true)
    public List<Image> 인기사진() {
        List<Image> images = imageRepository.mPopular();

        images.forEach(image -> {
            image.setS3PostImageUrl(s3UrlResolver.resolve(image.getPostImageUrl()));
        });

        return images;
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "imageStory",
            key = "#principalId + '_' + #pageable.pageNumber",
            cacheManager = "cacheManager"
    )
    public StoryResponseDto 이미지스토리(int principalId, Pageable pageable) {

        Page<Image> images = imageRepository.mStory(principalId, pageable);

        // 1. Image -> StoryDto 가공
        List<StoryDto> storyDtos = images.getContent().stream().map(image -> {
            return StoryDto.builder()
                    .id(image.getId())
                    .caption(image.getCaption())
                    .s3PostImageUrl(s3UrlResolver.resolve(image.getPostImageUrl()))
                    .username(image.getUser().getUsername())
                    .s3ProfileImageUrl(s3UrlResolver.resolve(image.getUser().getProfileImageUrl()))
                    .likeCount(image.getLikes().size())
                    .likeState(image.getLikes().stream().anyMatch(l -> l.getUser().getId() == principalId))
                    .comments(image.getComments().stream().map(c ->
                            StoryDto.CommentDto.builder()
                                    .id(c.getId())
                                    .content(c.getContent())
                                    .commentUsername(c.getUser().getUsername())
                                    .commentUserId(c.getUser().getId())
                                    .build()
                    ).collect(Collectors.toList()))
                    .build();
        }).collect(Collectors.toList());

        // 2. 가공된 리스트와 페이징 정보를 DTO에 담아서 리턴
        return StoryResponseDto.builder()
                .content(storyDtos)
                .last(images.isLast())
                .number(images.getNumber())
                .build();
    }

//    @Value("${file.path}")
//    private String uploadFolder;

    @Transactional
    @CacheEvict(value = "imageStory", allEntries = true)
    public void 사진업로드(ImageUploadDto imageUploadDto, PrincipalDetails principalDetails) {

        // 파일 확장자 체크 (MIME 타입 확인)
        String contentType = imageUploadDto.getFile().getContentType();

        // image/jpeg, image/png, image/gif 등 "image/"로 시작하는지 확인
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException("이미지 파일만 업로드 가능합니다.");
        }

        UUID uuid = UUID.randomUUID();
        String imageFileName = uuid + "_" + imageUploadDto.getFile().getOriginalFilename();

        try (InputStream is = imageUploadDto.getFile().getInputStream()) {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageFileName)
                    .contentType(contentType)
                    .acl("public-read") // 필수! 그래야 퍼블릭 URL 접근 가능
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(is, imageUploadDto.getFile().getSize()));

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("사진 업로드에 실패했습니다.");
        }

        /** 이전 로컬 저장방식
        Path imageFilePath = Paths.get(uploadFolder + imageFileName);

        // 통신, I/O -> 예외가 발생할 수 있다.
        try {
            Files.write(imageFilePath, imageUploadDto.getFile().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        // image 테이블에 저장
        Image image = imageUploadDto.toEntity(imageFileName, principalDetails.getUser());
        Image imageEntity = imageRepository.save(image);

    }

}
