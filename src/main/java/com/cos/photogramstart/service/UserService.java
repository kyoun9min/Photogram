package com.cos.photogramstart.service;

import com.cos.photogramstart.domain.subscribe.SubscribeRepository;
import com.cos.photogramstart.domain.user.User;
import com.cos.photogramstart.domain.user.UserRepository;
import com.cos.photogramstart.handler.ex.CustomApiException;
import com.cos.photogramstart.handler.ex.CustomException;
import com.cos.photogramstart.handler.ex.CustomValidationApiException;
import com.cos.photogramstart.s3.component.S3UrlResolver;
import com.cos.photogramstart.web.dto.user.UserProfileDto;
import com.cos.photogramstart.web.dto.user.UserSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    private final SubscribeRepository subscribeRepository;

    private final PasswordEncoder bCryptPasswordEncoder;

    private final S3Client s3Client;

    private final S3UrlResolver s3UrlResolver;

    @Value("${s3.bucket}")
    private String bucketName;

//    @Value("${file.path}")
//    private String uploadFolder;

    @Transactional
    public User 회원프로필사진변경(int principalId, MultipartFile profileImageFile) {

        UUID uuid = UUID.randomUUID();
        String imageFileName = uuid + "_" + profileImageFile.getOriginalFilename();

        try (InputStream is = profileImageFile.getInputStream()) {


            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageFileName)
                    .acl("public-read") // 퍼블릭 접근
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(is, profileImageFile.getSize()));

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomApiException("사진 업로드에 실패했습니다.");
        }

        /** 로컬 파일 업로드
        Path imageFilePath = Paths.get(uploadFolder + imageFileName);

        // 통신, I/O -> 예외가 발생할 수 있다.
        try {
            Files.write(imageFilePath, profileImageFile.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        User userEntity = userRepository.findById(principalId).orElseThrow(() -> new CustomApiException("유저를 찾을 수 없습니다."));
        userEntity.setProfileImageUrl(imageFileName); // DB에는 파일명만 저장

        return userEntity;
    }

    @Transactional(readOnly = true)
    public UserProfileDto 회원프로필(int pageUserId, int principalId) {

        UserProfileDto dto = new UserProfileDto();

        // SELECT * FROM image WHERE userId = :userId;
        User userEntity = userRepository.findById(pageUserId).orElseThrow(() -> new CustomException("해당 프로필 페이지는 없는 페이지입니다."));

        int subscribeState = subscribeRepository.mSubscribeState(principalId, pageUserId);
        int subscribeCount = subscribeRepository.mSubscribeCount(pageUserId);

        userEntity.getImages().forEach(image -> {
            image.setLikeCount(image.getLikes().size());
            image.setS3PostImageUrl(s3UrlResolver.resolve(image.getPostImageUrl()));
        });

        userEntity.setS3ProfileImageUrl(s3UrlResolver.resolve(userEntity.getProfileImageUrl()));

        dto.setUser(userEntity);
        dto.setPageOwnerState(pageUserId == principalId);
        dto.setImageCount(userEntity.getImages().size());
        dto.setSubscribeState(subscribeState == 1);
        dto.setSubscribeCount(subscribeCount);

        return dto;
    }

    @Transactional
    public User 회원수정(int id, User user) {
        // 1. 영속화
        User userEntity = userRepository.findById(id).orElseThrow(() -> new CustomValidationApiException("찾을 수 없는 id입니다."));

        // 2. 영속화된 오브젝트를 수정 - 더티체킹(업데이트 완료)
        userEntity.setName(user.getName());

        String rawPassword = user.getPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);

        userEntity.setPassword(encPassword);
        userEntity.setBio(user.getBio());
        userEntity.setWebsite(user.getWebsite());
        userEntity.setPhone(user.getPhone());
        userEntity.setGender(user.getGender());

        return userEntity;
    }

    @Transactional(readOnly = true)
    public List<UserSearchDto> 회원검색(String name) {

        List<User> users = userRepository.findByNameContaining(name);

        // 엔티티 리스트를 DTO 리스트로 변환
        return users.stream().map(user -> UserSearchDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .s3ProfileImageUrl(s3UrlResolver.resolve(user.getProfileImageUrl()))
                .build()
        ).collect(Collectors.toList());
    }

    public String getProfileImageUrl(User user) {
        return s3UrlResolver.resolve(user.getProfileImageUrl());
    }

}
