package com.cos.photogramstart.config;

import com.cos.photogramstart.config.oauth.OAuth2DetailsService;
import com.cos.photogramstart.handler.CustomAuthFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@RequiredArgsConstructor
@Configuration // IoC
public class SecurityConfig {

    private final OAuth2DetailsService oAuth2DetailsService;

    private final CustomAuthFailureHandler customAuthFailureHandler;

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {

        http.csrf(c -> c.disable());

        http.authorizeHttpRequests(authorize -> authorize
//                .requestMatchers("/", "/user/**", "/image/**", "/subscribe/**", "/comment/**", "/api/**").authenticated()
                .anyRequest()
                .permitAll()
        );

        http.formLogin(form -> form
                .loginPage("/auth/signin") // GET
                .loginProcessingUrl("/auth/signin") // POST
                .defaultSuccessUrl("/")
                .failureHandler(customAuthFailureHandler)
        );

        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/signin")
                .defaultSuccessUrl("/image/story", true)
                .failureUrl("/auth/signin?oauth2error")
                .userInfoEndpoint(userInfoEndpointConfig ->
                        userInfoEndpointConfig.userService(oAuth2DetailsService))
        );

        return http.build();
    }
}
