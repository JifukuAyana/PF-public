package com.example.demo.controller.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.constant.AuthorityKind;
import com.example.demo.constant.UrlConst;
import com.example.demo.service.LikeService;
import com.example.demo.service.UserService;

@Controller
public class DashController {

    private final LikeService likeService;
    private final UserService userService;

    public DashController(LikeService likeService, UserService userService) {
        this.likeService = likeService;
        this.userService = userService;
    }

    @GetMapping(UrlConst.DASH)
    public String view(@AuthenticationPrincipal User user, Model model) {
        var hasUserManageAuth = user.getAuthorities().stream()
            .allMatch(authority -> authority.getAuthority()
                .equals(AuthorityKind.ITEM_AND_USER_MANAGER.getAuthorityKind()));
        model.addAttribute("hasUserManageAuth", hasUserManageAuth);
        
        // ログイン回数ランキング
        model.addAttribute("yearlyRanking", userService.getYearlyRankingLoginIds());
        model.addAttribute("monthlyRanking", userService.getMonthlyRankingLoginIds());

        // 🔹 年間・月間いいね履歴ベースランキング
        model.addAttribute("yearlyLikeRanking", likeService.getYearlyLikeRanking());
        model.addAttribute("monthlyLikeRanking", likeService.getMonthlyLikeRanking());

        return "admin/dash";
    }
}
