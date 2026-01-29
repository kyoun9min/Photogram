package com.cos.photogramstart.service;

import com.cos.photogramstart.config.auth.PrincipalDetails;
import com.cos.photogramstart.domain.image.Image;
import com.cos.photogramstart.domain.image.ImageRepository;
import com.cos.photogramstart.handler.ex.CustomException;
import com.cos.photogramstart.s3.component.S3UrlResolver;
import com.cos.photogramstart.web.dto.image.ImageUploadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

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
    public Page<Image> 이미지스토리(int principalId, Pageable pageable) {

        Page<Image> images = imageRepository.mStory(principalId, pageable);

        // 2번(cos)으로 로그인
        // images에 좋아요 상태 담기
        images.forEach(image -> {

            image.setLikeCount(image.getLikes().size());

            image.getLikes().forEach(likes -> {
                if (likes.getUser().getId() == principalId) { // 해당 이미지에 좋아요한 사람들을 찾아서, 현재 로긴한 사람이 좋아요 한 것인지 비교
                    image.setLikeState(true);
                }
            });

            image.setS3PostImageUrl(s3UrlResolver.resolve(image.getPostImageUrl())); // 게시물 이미지 S3 URL로 변환
            image.getUser().setS3ProfileImageUrl(s3UrlResolver.resolve(image.getUser().getProfileImageUrl())); // 작성자 프로필 이미지 S3 URL로 변환
        });

        return images;
    }

//    @Value("${file.path}")
//    private String uploadFolder;

    @Transactional
    public void 사진업로드(ImageUploadDto imageUploadDto, PrincipalDetails principalDetails) {

        UUID uuid = UUID.randomUUID();
        String imageFileName = uuid + "_" + imageUploadDto.getFile().getOriginalFilename();

        try (InputStream is = imageUploadDto.getFile().getInputStream()) {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageFileName)
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
