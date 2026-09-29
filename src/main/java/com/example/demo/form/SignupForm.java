package com.example.demo.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

/**
 * ユーザー登録画面 form
 */
@Data
public class SignupForm {

    /** ログインID */
    @NotBlank(message = "{error.required}")
    @Size(max = 255, message = "{error.name}")
    private String loginId;

    /** メールアドレス */
    @NotBlank(message = "{error.required}")
    @Size(max = 255, message = "{error.mail.length}")
    @Email(message = "{error.mail.format}")
    private String mail;

    /** パスワード */
    @NotBlank(message = "{error.required}")
    private String password;

    @AssertTrue(message = "{error.password.length}")
    public boolean isPasswordLengthValid() {
        return password == null || password.isEmpty() || (password.length() >= 8 && password.length() <= 32);
    }

    @AssertTrue(message = "{error.password.format}")
    public boolean isPasswordFormatValid() {
        return password == null || password.isEmpty() || password.matches("^[a-z0-9_-]*$");
    }

    /** ユーザー権限（管理者 or 一般） */
    private String authority;

    /** ステータス（アクセス許可 or 拒否） */
    private Integer access;

    /** ふりがな（255文字以内） */
    @Size(max = 255, message = "{error.furigana.length}")
    private String furigana;

    @AssertTrue(message = "{error.required}")
    public boolean isFuriganaRequired() {
        if ("1".equals(authority)) return true;
        return furigana != null && !furigana.trim().isEmpty();
    }

    @AssertTrue(message = "{error.furigana.format}")
    public boolean isFuriganaFormatValid() {
        if ("1".equals(authority)) return true;
        return furigana == null || furigana.trim().isEmpty() || furigana.matches("^[\\u3040-\\u309Fー]+$");
    }

    /** 性別（1:男性, 2:女性, 3:その他） */
    private Integer gender = 3;
    
    @AssertTrue(message = "{error.gender.invalid}")
    public boolean isGenderValid() {
        if ("1".equals(authority)) return true;
        return gender != null && (gender == 1 || gender == 2 || gender == 3);
    }


    @AssertTrue(message = "{error.gender.required}")
    public boolean isGenderRequired() {
        if ("1".equals(authority)) return true;
        return gender != null;
    }

    /** 年齢（0～999） */
    private Integer age;

    @AssertTrue(message = "{error.required}")
    public boolean isAgeRequired() {
        if ("1".equals(authority)) return true;
        return age != null;
    }

    @AssertTrue(message = "{error.age.range}")
    public boolean isAgeValid() {
        if ("1".equals(authority)) return true;
        if (age == null) return true;
        return age >= 0 && age <= 999;
    }

    /** 自己紹介（必須・1500文字以内） */
    private String introduction;

    @AssertTrue(message = "{error.required}")
    public boolean isIntroductionRequired() {
        if ("1".equals(authority)) return true;
        return introduction != null && !introduction.trim().isEmpty();
    }

    @AssertTrue(message = "{error.introduction}")
    public boolean isIntroductionLengthValid() {
        if ("1".equals(authority)) return true;
        return introduction == null || introduction.length() <= 1500;
    }

    /** 写真挿入 */
    private MultipartFile profileImage;

    /** デフォルト写真挿入 */
    private String profileImageName;
    

    @AssertTrue(message = "{error.profileImage.size}")
    public boolean isProfileImageSizeValid() {
        return profileImage == null || profileImage.getSize() <= (2 * 1024 * 1024);
    }

    @AssertTrue(message = "{error.profileImage.required}")
    public boolean isProfileImageSelected() {
        if ("1".equals(authority)) return true;
        return profileImage != null && !profileImage.isEmpty();
    }

}