package com.cos.photogramstart.service;

import com.cos.photogramstart.config.auth.PrincipalDetails;
import com.cos.photogramstart.domain.image.Image;
import com.cos.photogramstart.domain.image.ImageRepository;
import com.cos.photogramstart.domain.user.User;
import com.cos.photogramstart.handler.ex.CustomException;
import com.cos.photogramstart.s3.component.S3UrlResolver;
import com.cos.photogramstart.web.dto.image.ImageUploadDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private S3Client s3Client; // S3 호출을 가짜로 가로챔

    @Mock
    private S3UrlResolver s3UrlResolver;

    @InjectMocks
    private ImageService imageService;

    @Test
    @DisplayName("사진 업로드 성공 테스트 (S3 & DB)")
    void 사진업로드_성공() throws Exception {
        // given
        // 1. 가짜 파일 생성 (MockMultipartFile)
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image content".getBytes());

        ImageUploadDto uploadDto = new ImageUploadDto();
        uploadDto.setFile(mockFile);
        uploadDto.setCaption("테스트 이미지 입니다.");

        // 2. 가짜 유저 정보 설정
        User user = User.builder().id(1).username("cos").build();
        PrincipalDetails principalDetails = new PrincipalDetails(user);

        // 3. Mock 설정: repository.save() 호출 시 결과 반환
        given(imageRepository.save(any(Image.class))).willReturn(new Image());

        // when
        imageService.사진업로드(uploadDto, principalDetails);

        // then
        // 1. S3Client의 putObject 메서드가 호출되었는지 확인
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // 2. Repository의 save 메서드가 호출되었는지 확인
        verify(imageRepository, times(1)).save(any(Image.class));
    }

    @Test
    @DisplayName("S3 업로드 실패 시 예외 발생 테스트")
    void 사진업로드_S3실패() throws Exception {
        // given
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        ImageUploadDto uploadDto = new ImageUploadDto();
        uploadDto.setFile(mockFile);

        User user = User.builder().id(1).build();
        PrincipalDetails principalDetails = new PrincipalDetails(user);

        // S3Client가 예외를 던지도록 설정
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willThrow(new RuntimeException("S3 Error"));

        // when & then
        assertThatThrownBy(() -> {
            imageService.사진업로드(uploadDto, principalDetails);
        }).isInstanceOf(CustomException.class)
                .hasMessageContaining("사진 업로드에 실패했습니다.");
    }

    @Test
    @DisplayName("이미지가 아닌 파일을 업로드하면 CustomException이 발생한다")
    void 사진업로드_이미지아님_실패() {
        // given
        // MIME 타입을 "text/plain" (텍스트 파일)으로 설정
        MockMultipartFile textFile = new MockMultipartFile(
                "file", "virus.txt", "text/plain", "malicious content".getBytes());

        ImageUploadDto uploadDto = new ImageUploadDto();
        uploadDto.setFile(textFile);

        User user = User.builder().id(1).build();
        PrincipalDetails principalDetails = new PrincipalDetails(user);

        // when & then
        assertThatThrownBy(() -> {
            imageService.사진업로드(uploadDto, principalDetails);
        }).isInstanceOf(CustomException.class)
                .hasMessageContaining("이미지 파일만 업로드 가능합니다.");

        // 검증: 파일이 이미지가 아니므로 S3 업로드나 DB 저장이 한 번도 호출되지 않아야 함!
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(imageRepository, never()).save(any(Image.class));
    }

}