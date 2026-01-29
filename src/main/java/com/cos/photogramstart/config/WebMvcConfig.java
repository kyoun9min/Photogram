package com.cos.photogramstart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

//@Configuration 로컬 이미지파일 업로드용. s3버킷 쓰니까 이제 안씀
public class WebMvcConfig implements WebMvcConfigurer { // web 설정 파일

    @Value("${file.path}")
    private String uploadFolder;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry);


        // file:///C:/Users/Romero/Desktop/springworkspace/upload/
        registry
                .addResourceHandler("/upload/**") // jsp페이지에서 /upload/** 이런 주소 패턴이 나오면 발동
                .addResourceLocations("file:" + uploadFolder)
                .setCachePeriod(60 * 10 * 6) // 이미지 캐싱 1시간 (60 * 10 * 6초로 계산)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }
}
