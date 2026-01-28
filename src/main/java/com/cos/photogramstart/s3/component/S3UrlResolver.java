package com.cos.photogramstart.s3.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3UrlResolver {

    @Value("${s3.bucket}")
    private String bucket;

    @Value("${s3.region}")
    private String region;

    public String resolve(String key) {
        if (key == null || key.isBlank()) return null;
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}
