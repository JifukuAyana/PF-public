package com.example.demo.controller.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.UserInfo;
import com.example.demo.form.UserEditForm;
import com.example.demo.repository.UserInfoRepository;
import com.example.demo.repository.UserLikeHistoryRepository;
import com.example.demo.service.UserService;

@Controller
public class UserListController {

    private final UserService userService;
    private final UserInfoRepository userInfoRepository;
    private final UserDetailsService userDetailsService;
    private final UserLikeHistoryRepository userLikeHistoryRepository;

    public UserListController(UserService userService,
                              UserInfoRepository userInfoRepository,
                              UserDetailsService userDetailsService,
                              UserLikeHistoryRepository userLikeHistoryRepository) {
        this.userService = userService;
        this.userInfoRepository = userInfoRepository;
        this.userDetailsService = userDetailsService;
        this.userLikeHistoryRepository = userLikeHistoryRepository;
    }

    @GetMapping("/userList")
    public String view(
            Model model,
            @ModelAttribute("userEditForm") UserEditForm userEditForm,
            @ModelAttribute("editTargetLoginId") String editTargetLoginId,
            @ModelAttribute("org.springframework.validation.BindingResult.userEditForm") BindingResult bindingResult
    ) {
        model.addAttribute("users", userService.getActiveUsers());

        if (!model.containsAttribute("userEditForm") && editTargetLoginId != null && !editTargetLoginId.isEmpty()) {
            UserInfo user = userService.getUserById(editTargetLoginId);
            if (user != null) {
                UserEditForm form = new UserEditForm();
                form.setLoginId(user.getLoginId());
                form.setMail(user.getMail());
                form.setAccess(user.getAccess());
                form.setAuthority(user.getAuthority());
                form.setFurigana(user.getFurigana());
                form.setGender(user.getGender());
                form.setAge(user.getAge());
                form.setIntroduction(user.getIntroduction());
                form.setOriginalLoginId(user.getLoginId());
                model.addAttribute("userEditForm", form);
            }
        }

//        if (editTargetLoginId != null && !editTargetLoginId.isEmpty()) {
//            model.addAttribute("showEditModal", true);
//        }

        return "admin/userList";
    }

    @PostMapping("/like")
    @ResponseBody
    public String likeUser(@RequestParam String loginId) {
        int newLikes = userService.incrementLikes(loginId);
        return String.valueOf(newLikes);
    }

    @GetMapping("/users/detail")
    @ResponseBody
    public ResponseEntity<UserInfo> getUserDetail(@RequestParam String loginId) {
        UserInfo user = userService.getUserById(loginId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @Transactional
    @PostMapping("/userList")
    public String postUserList(
            @Validated @ModelAttribute("userEditForm") UserEditForm form,
            BindingResult result,
            Model model
    ) {
    	  // ✅ 画像サイズバリデーション（2MB）
    	if (form.getProfileImage() != null && !form.getProfileImage().isEmpty()) {
    	    if (form.getProfileImage().getSize() > 2 * 1024 * 1024) {
    	        result.rejectValue("profileImage", "error.profileImage.size", "プロフィール画像は2MB以内で選択してください。");
    	        model.addAttribute("isError", true);
    	        model.addAttribute("message", "common.formError");
    	        model.addAttribute("users", userService.getActiveUsers());
    	        model.addAttribute("userEditForm", form);
    	        model.addAttribute("editTargetLoginId", form.getOriginalLoginId());
    	        model.addAttribute("showEditModal", true);
    	        return "admin/userList";
    	    }
    	}

     // バリデーションエラーがある場合の処理
        if (result.hasErrors()) {
            model.addAttribute("showEditModal", true);
            model.addAttribute("editTargetLoginId", form.getOriginalLoginId());
            model.addAttribute("users", userService.getActiveUsers());
            model.addAttribute("isError", true);
            model.addAttribute("message", "項目チェックエラーがあります。");
            return "admin/userList";
        }

        Optional<UserInfo> existingOpt = userInfoRepository.findById(form.getOriginalLoginId());
        if (existingOpt.isEmpty()) {
            model.addAttribute("isError", true);
            model.addAttribute("message", "ユーザーが存在しません。");
            model.addAttribute("users", userService.getActiveUsers());
            return "admin/userList";
        }

        UserInfo existing = existingOpt.get();
        boolean loginIdChanged = !existing.getLoginId().equals(form.getLoginId());

        if (loginIdChanged && userInfoRepository.existsById(form.getLoginId())) {
            model.addAttribute("isError", true);
            model.addAttribute("message", "既に登録されているログインIDです。");
            model.addAttribute("userEditForm", form);
            model.addAttribute("showEditModal", true);
            model.addAttribute("editTargetLoginId", form.getOriginalLoginId());
            model.addAttribute("users", userService.getActiveUsers());
            return "admin/userList";
        }

        MultipartFile profileImage = form.getProfileImage();
        String newFileName = existing.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String ext = profileImage.getOriginalFilename()
                        .substring(profileImage.getOriginalFilename().lastIndexOf(".")).toLowerCase();
                newFileName = form.getLoginId() + ext;
                Path uploadDir = Paths.get("upload-dir/profile-images");
                if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
                Files.copy(profileImage.getInputStream(), uploadDir.resolve(newFileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                model.addAttribute("isError", true);
                model.addAttribute("message", "画像保存中にエラーが発生しました。");
                return "admin/userList";
            }
        }

        // 現在ログイン中のユーザーIDを取得
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        String currentLoginId = currentAuth.getName();

        if (loginIdChanged) {
            UserInfo newUser = new UserInfo();
            newUser.setLoginId(form.getLoginId());
            newUser.setMail(form.getMail());
            newUser.setPassword(form.getPassword() != null && !form.getPassword().isEmpty()
                    ? new BCryptPasswordEncoder().encode(form.getPassword())
                    : existing.getPassword());
            newUser.setAuthority(form.getAuthority());
            newUser.setAccess(form.getAccess());
            newUser.setFurigana(form.getFurigana());
            newUser.setGender(form.getGender());
            newUser.setAge(form.getAge());
            newUser.setIntroduction(form.getIntroduction());
            newUser.setProfileImage(newFileName);
            newUser.setDeleted(false);
            newUser.setLikes(existing.getLikes());

            userInfoRepository.save(newUser);
            userLikeHistoryRepository.updateLoginId(existing.getLoginId(), newUser.getLoginId());
            userInfoRepository.delete(existing);

            // ✅ ログイン中のユーザー本人が変更された場合だけ認証情報を更新する
            if (existing.getLoginId().equals(currentLoginId)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getLoginId());
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        currentAuth.getCredentials(),
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }
        } else {
            existing.setMail(form.getMail());
            if (form.getPassword() != null && !form.getPassword().isEmpty()) {
                existing.setPassword(new BCryptPasswordEncoder().encode(form.getPassword()));
            }
            existing.setAuthority(form.getAuthority());
            existing.setAccess(form.getAccess());
            existing.setFurigana(form.getFurigana());
            existing.setGender(form.getGender());
            existing.setAge(form.getAge());
            existing.setIntroduction(form.getIntroduction());
            existing.setProfileImage(newFileName);
            userInfoRepository.save(existing);
        }

        model.addAttribute("editTargetLoginId", form.getLoginId());
        model.addAttribute("showEditModal", true);
        model.addAttribute("isError", false);
        model.addAttribute("message", "更新が完了しました。");
        model.addAttribute("users", userService.getActiveUsers());
        return "admin/userList";
    }
}