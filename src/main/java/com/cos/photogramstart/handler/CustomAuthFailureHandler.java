package com.cos.photogramstart.handler;

import com.cos.photogramstart.util.Script;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // 브라우저에게 HTML(스크립트)을 돌려줄 준비
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();

        // 아이디 틀림(UsernameNotFoundException) 혹은 비번 틀림(BadCredentialsException) 구분 없이 처리
        out.print(Script.back("아이디 또는 비밀번호가 잘못되었습니다."));
        out.flush();
    }
}