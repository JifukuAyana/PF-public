package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.constant.AuthorityKind;

@ControllerAdvice
public class SidebarController {

    @ModelAttribute
    public void setSidebarAttributes(@AuthenticationPrincipal User user, Model model) {
        if (user != null) {
            boolean hasUserManageAuth = user.getAuthorities().stream()
                .allMatch(authority -> authority.getAuthority()
                    .equals(AuthorityKind.ITEM_AND_USER_MANAGER.getAuthorityKind()));
            model.addAttribute("hasUserManageAuth", hasUserManageAuth);
        }
    }
}
