package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.UserInfo;
import com.example.demo.form.ProfileEditForm;
import com.example.demo.repository.UserInfoRepository;
import com.example.demo.repository.UserLikeHistoryRepository;

@Controller
public class ProfileController {

    private final UserInfoRepository userInfoRepository;
    private final UserLikeHistoryRepository userLikeHistoryRepository;
    private final UserDetailsService userDetailsService;

    public ProfileController(UserInfoRepository userInfoRepository,
                             UserLikeHistoryRepository userLikeHistoryRepository,
                             UserDetailsService userDetailsService) {
        this.userInfoRepository = userInfoRepository;
        this.userLikeHistoryRepository = userLikeHistoryRepository;
        this.userDetailsService = userDetailsService;
    }

    // GET: プロフィール画面表示
    @GetMapping("/profile")
    public String view(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginId = auth.getName();

        Optional<UserInfo> userOpt = userInfoRepository.findById(loginId);
        userOpt.ifPresent(user -> {
            ProfileEditForm form = new ProfileEditForm();
            form.setLoginId(user.getLoginId());
            form.setOriginalLoginId(user.getLoginId());
            form.setFurigana(user.getFurigana());
            form.setGender(user.getGender());
            form.setAge(user.getAge());
            form.setIntroduction(user.getIntroduction());
            form.setProfileImageName(user.getProfileImage());
            model.addAttribute("profileEditForm", form);
            model.addAttribute("user", user);
        });

        return "profile";
    }

 // POST: プロフィール更新
    @PostMapping("/profile")
    @Transactional
    public String updateProfile(
            @Validated @ModelAttribute("profileEditForm") ProfileEditForm form,
            BindingResult bindingResult,
            Model model) {

        // ✅ 1回だけMultipartFileを定義して再利用
        MultipartFile profileImage = form.getProfileImage();

        // ✅ この位置で画像サイズのバリデーション（2MB超過時にエラー付加）
        if (profileImage != null && !profileImage.isEmpty()) {
            if (profileImage.getSize() > 2 * 1024 * 1024) {
                bindingResult.rejectValue("profileImage", "error.profileImage.size");
            }
        }

        // ✅ バリデーションエラーがあれば即戻す（エラーメッセージ付き）
        if (bindingResult.hasErrors()) {
            model.addAttribute("isError", true);
            model.addAttribute("message", "common.formError");

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String loginId = auth.getName();
            userInfoRepository.findById(loginId).ifPresent(user -> model.addAttribute("user", user));

            return "profile";
        }

        // ✅ ログインユーザー取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginId = auth.getName();
        Optional<UserInfo> userOpt = userInfoRepository.findById(loginId);

        if (userOpt.isEmpty()) {
            model.addAttribute("isError", true);
            model.addAttribute("message", "common.formError");
            return "profile";
        }

        UserInfo user = userOpt.get();
        model.addAttribute("user", user);

        // ✅ loginId 変更された場合の処理
        if (!form.getLoginId().equals(form.getOriginalLoginId())) {
            if (userInfoRepository.existsById(form.getLoginId())) {
                model.addAttribute("isError", true);
                model.addAttribute("message", "signup.existedLoginId");
                model.addAttribute("profileEditForm", form);
                return "profile";
            }

            UserInfo newUser = new UserInfo();
            newUser.setLoginId(form.getLoginId());
            newUser.setMail(user.getMail());
            newUser.setFurigana(form.getFurigana());
            newUser.setGender(form.getGender());
            newUser.setAge(form.getAge());
            newUser.setIntroduction(form.getIntroduction());
            newUser.setProfileImage(user.getProfileImage()); // 後で上書きするかも
            newUser.setPassword(user.getPassword());
            newUser.setAuthority(user.getAuthority());
            newUser.setAccess(user.getAccess());
            newUser.setDeleted(false);
            newUser.setLikes(user.getLikes());

            // ✅ プロフィール画像保存
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    String ext = profileImage.getOriginalFilename()
                            .substring(profileImage.getOriginalFilename().lastIndexOf(".")).toLowerCase();
                    String newFileName = form.getLoginId() + ext;

                    Path uploadDir = Paths.get("upload-dir/profile-images");
                    if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

                    Files.copy(profileImage.getInputStream(),
                            uploadDir.resolve(newFileName),
                            StandardCopyOption.REPLACE_EXISTING);

                    newUser.setProfileImage(newFileName);
                } catch (IOException e) {
                    model.addAttribute("isError", true);
                    model.addAttribute("message", "error.profileImage.save");
                    return "profile";
                }
            }

            userInfoRepository.save(newUser);
            userLikeHistoryRepository.updateLoginId(user.getLoginId(), newUser.getLoginId());
            userInfoRepository.delete(user);

            // ✅ 認証情報更新
            UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getLoginId());
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    auth.getCredentials(),
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            // ✅ 再描画用Form
            ProfileEditForm newForm = new ProfileEditForm();
            newForm.setLoginId(newUser.getLoginId());
            newForm.setOriginalLoginId(newUser.getLoginId());
            newForm.setFurigana(newUser.getFurigana());
            newForm.setGender(newUser.getGender());
            newForm.setAge(newUser.getAge());
            newForm.setIntroduction(newUser.getIntroduction());
            newForm.setProfileImageName(newUser.getProfileImage());

            model.addAttribute("profileEditForm", newForm);
            model.addAttribute("user", newUser);
            model.addAttribute("message", "update.success");
            model.addAttribute("isError", false);
            return "profile";
        }

        // ✅ loginId変更がない場合の更新処理
        user.setFurigana(form.getFurigana());
        user.setGender(form.getGender());
        user.setAge(form.getAge());
        user.setIntroduction(form.getIntroduction());

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String ext = profileImage.getOriginalFilename()
                        .substring(profileImage.getOriginalFilename().lastIndexOf(".")).toLowerCase();
                String newFileName = loginId + ext;

                Path uploadDir = Paths.get("upload-dir/profile-images");
                if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

                Files.copy(profileImage.getInputStream(),
                        uploadDir.resolve(newFileName),
                        StandardCopyOption.REPLACE_EXISTING);

                user.setProfileImage(newFileName);
            } catch (IOException e) {
                model.addAttribute("isError", true);
                model.addAttribute("message", "error.profileImage.save");
                return "profile";
            }
        }

        userInfoRepository.save(user);

        // ✅ 更新後の表示用フォームを再生成
        ProfileEditForm newForm = new ProfileEditForm();
        newForm.setLoginId(user.getLoginId());
        newForm.setOriginalLoginId(user.getLoginId());
        newForm.setFurigana(user.getFurigana());
        newForm.setGender(user.getGender());
        newForm.setAge(user.getAge());
        newForm.setIntroduction(user.getIntroduction());
        newForm.setProfileImageName(user.getProfileImage());

        model.addAttribute("profileEditForm", newForm);
        model.addAttribute("message", "update.success");
        model.addAttribute("isError", false);
        return "profile";
    }
}
