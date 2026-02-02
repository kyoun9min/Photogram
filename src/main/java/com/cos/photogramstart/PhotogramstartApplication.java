package com.cos.photogramstart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession // 이 어노테이션이 Redis 세션 관리를 활성화
@SpringBootApplication
public class PhotogramstartApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(PhotogramstartApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(PhotogramstartApplication.class, args);
	}

}
