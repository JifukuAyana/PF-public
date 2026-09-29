package com.example.demo.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.UserInfo;
import com.example.demo.service.UserService;

@Controller
public class DeleteListController {

    @Autowired
    private UserService userService;

    @GetMapping("/deleteList")
    public String view(Model model) {
        // 削除済みユーザー（deleted = 1）を取得
        List<UserInfo> deletedUsers = userService.getDeletedUsers();
        model.addAttribute("deletedUsers", deletedUsers);
        return "admin/deleteList";
    }
    
    @PostMapping("/deleteList/permanentDelete")
    public String permanentlyDeleteUser(@RequestParam("loginId") String loginId) {
        userService.permanentlyDeleteUser(loginId);
        return "redirect:/deleteList";
    }
}
