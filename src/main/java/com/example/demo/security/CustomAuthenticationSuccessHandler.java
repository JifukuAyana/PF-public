package com.example.demo.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.constant.AuthorityKind;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String redirectUrl = "/dashboard"; // デフォルトは一般ユーザー

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (AuthorityKind.ITEM_AND_USER_MANAGER.getAuthorityKind().equals(authority.getAuthority())) {
                redirectUrl = "/dash"; // 管理者なら dash
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
