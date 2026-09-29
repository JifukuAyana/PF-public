package com.example.demo.controller.api;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.UserInfo;
import com.example.demo.repository.UserInfoRepository;

@RestController
@RequestMapping("/api/user")
public class UserApiController {
    private final UserInfoRepository userInfoRepository;

    public UserApiController(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UserInfo updatedUser) {
        Optional<UserInfo> existingUserOpt = userInfoRepository.findById(updatedUser.getLoginId());

        if (existingUserOpt.isPresent()) {
            UserInfo user = existingUserOpt.get();
            
            // 名前（ログインID）は変更不可とする（必要なら変更ロジックを追加）
            // user.setLoginId(updatedUser.getLoginId());

            // メールアドレス更新
            user.setMail(updatedUser.getMail());

            // パスワード更新（未入力なら変更しない）
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(updatedUser.getPassword()); // ※パスワードはハッシュ化推奨
            }

            userInfoRepository.save(user); // データベースに保存
            return ResponseEntity.ok().body("{\"success\": true}");
        } else {
            return ResponseEntity.badRequest().body("{\"success\": false}");
        }
    }
}
