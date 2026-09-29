package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.service.LikeService;

@Controller
public class DashboardController {

    private final LikeService likeService;

    public DashboardController(LikeService likeService) {
        this.likeService = likeService;
    }

    @GetMapping("/dashboard")
    public String viewMyLikes(@AuthenticationPrincipal User user, Model model) {
        String loginId = user.getUsername();

        int yearlyLikes = likeService.getYearlyLikesCount(loginId);
        int monthlyLikes = likeService.getMonthlyLikesCount(loginId);

        model.addAttribute("yearlyLikes", yearlyLikes);
        model.addAttribute("monthlyLikes", monthlyLikes);

        return "dashboard";
    }
	
}
