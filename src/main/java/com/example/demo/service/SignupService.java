package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.dozer.Mapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.UserInfo;
import com.example.demo.form.SignupForm;
import com.example.demo.repository.UserInfoRepository;

import lombok.RequiredArgsConstructor;

/**
 * ユーザー登録画面 service
 * 
 * @author ayyyy
 */

@Service
@RequiredArgsConstructor  //private finalで宣言したインスタンスに対してnewしたものを注入するコンストラクタを実装してくれる。  
public class SignupService {

    //ユーザー情報テーブルレポジトリ
    private final UserInfoRepository repository;

    /** Dozer Mapper */
    private final Mapper mapper;

    /** PasswordEncoder */
    private final PasswordEncoder passwordEncoder;

    /**
     * ユーザ情報テーブル 新規登録
     *
     * @param form 入力情報
     * @return 登録情報（ユーザー情報Entity）、既に同じユーザIDで登録がある場合はEmpty
     */
    public Optional<UserInfo> resistUserInfo(SignupForm form) {
        var userInfoExistedOpt = repository.findById(form.getLoginId());
        if (userInfoExistedOpt.isPresent()) {
            return Optional.empty();
        }

        var userInfo = mapper.map(form, UserInfo.class);
        var encodedPassword = passwordEncoder.encode(form.getPassword());
        userInfo.setPassword(encodedPassword);

        if (form.getAuthority() == null || form.getAuthority().isEmpty()) {
            userInfo.setAuthority("2"); // 一般にデフォルト
        } else {
            userInfo.setAuthority(form.getAuthority());
        }

        // 🔽 追加：フォームから取得したデータをセット
        userInfo.setMail(form.getMail());         // ステータス
        userInfo.setAccess(form.getAccess());         // ステータス
        userInfo.setFurigana(form.getFurigana());     // ふりがな
        
        // 性別（null 対策）
        Integer gender = form.getGender();
        if (gender != null) {
            userInfo.setGender(gender);
        } else {
            userInfo.setGender(3); // デフォルト「その他」
        }
        
        userInfo.setGender(form.getGender());        // 性別
        userInfo.setAge(form.getAge());              // 年齢
        userInfo.setIntroduction(form.getIntroduction()); // 自己紹介

        // プロフィール画像ファイル名を保存
        MultipartFile profileImage = form.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            String originalFileName = profileImage.getOriginalFilename();
            if (originalFileName != null && originalFileName.contains(".")) {
                String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String fileName = form.getLoginId() + extension;
                userInfo.setProfileImage(form.getProfileImageName()); // ✅文字列としてファイル名を設定
            }
        } else {
            userInfo.setProfileImage("default.jpg"); 
        }

        return Optional.of(repository.save(userInfo));
    }

    public String saveProfileImage(MultipartFile file, String loginId) throws IOException {
        String uploadDir = "upload-dir/profile-images/"; 
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }

        String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String filename = loginId + extension;

        Path filePath = Paths.get(uploadDir + filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

}
