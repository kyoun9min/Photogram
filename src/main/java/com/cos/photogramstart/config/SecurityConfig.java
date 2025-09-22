package com.cos.photogramstart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // IoC
public class SecurityConfig {

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {

        http.csrf(c -> c.disable());

        http.authorizeHttpRequests(authorize ->
                authorize.requestMatchers("/", "/user/**", "/image/**", "/subscribe/**", "/comment/**").authenticated()
                        .anyRequest().permitAll()
        );

        http.formLogin(form ->
                form.loginPage("/auth/signin")
                        .defaultSuccessUrl("/")
        );

        return http.build();
    }
}
