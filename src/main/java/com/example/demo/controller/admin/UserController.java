package com.example.demo.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {	
        this.userService = userService;
    }

    // 🔹 アクセス権の切り替え
    @PutMapping("/{loginId}/toggleAccess")
    public ResponseEntity<String> toggleAccess(@PathVariable String loginId) {
        boolean result = userService.toggleAccessStatus(loginId);  // ← fetch対応の安全なメソッド

        if (result) {
            return ResponseEntity.ok("ステータスを更新しました！");
        } else {
            return ResponseEntity.status(404).body("ユーザーが見つかりません");
        }
    }

    // 🔹 論理削除API（削除ボタン用）
    @DeleteMapping("/{loginId}/delete")
    public ResponseEntity<String> deleteUser(@PathVariable String loginId) {
        userService.deleteUser(loginId);
        return ResponseEntity.ok("ユーザーを削除しました");
    }

    // 🔹 復元API（復元ボタン用）
    @PutMapping("/{loginId}/restore")
    public ResponseEntity<String> restoreUser(@PathVariable String loginId) {
        userService.restoreUser(loginId);
        return ResponseEntity.ok("ユーザーを復元しました");
    }

    // 🔹 💀【完全削除】（完全に削除ボタン用）
    @DeleteMapping("/{loginId}/permanentDelete")
    public ResponseEntity<String> permanentlyDeleteUser(@PathVariable String loginId) {
        boolean success = userService.permanentlyDeleteUser(loginId);
        if (success) {
            return ResponseEntity.ok("ユーザーを完全に削除しました");
        } else {
            return ResponseEntity.badRequest().body("ユーザーの完全削除に失敗しました");
        }
    }

}
