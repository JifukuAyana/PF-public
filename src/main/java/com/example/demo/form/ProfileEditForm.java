package com.example.demo.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ProfileEditForm {

	  @NotBlank(message = "{error.loginId.required}")
	    @Size(max = 255, message = "{error.name}")
	    private String loginId;

	    private String authority;

	    @Size(max = 255, message = "{error.furigana.length}")
	    private String furigana;

//	    @AssertTrue(message = "{error.required}")
//	    public boolean isFuriganaRequired() {
//	        if ("1".equals(authority)) return true;
//	        return furigana != null && !furigana.trim().isEmpty();
//	    }

	    @AssertTrue(message = "{error.furigana.format}")
	    public boolean isFuriganaFormatValid() {
	        if ("1".equals(authority)) return true;
	        return furigana == null || furigana.trim().isEmpty() || furigana.matches("^[\\u3040-\\u309Fー]+$");
	    }

	    private Integer gender;

//	    @AssertTrue(message = "{error.gender.required}")
//	    public boolean isGenderRequired() {
//	        if ("1".equals(authority)) return true;
//	        return gender != null;
//	    }

	    @AssertTrue(message = "{error.gender.invalid}")
	    public boolean isGenderValid() {
	        if ("1".equals(authority)) return true;
	        return gender != null && (gender == 1 || gender == 2 || gender == 3);
	    }

	    private Integer age;

//	    @AssertTrue(message = "{error.required}")
//	    public boolean isAgeRequired() {
//	        if ("1".equals(authority)) return true;
//	        return age != null;
//	    }

	    @AssertTrue(message = "{error.age.range}")
	    public boolean isAgeValid() {
	        if ("1".equals(authority)) return true;
	        return age == null || (age >= 0 && age <= 999);
	    }

	    private String introduction;

//	    @AssertTrue(message = "{error.required}")
//	    public boolean isIntroductionRequired() {
//	        if ("1".equals(authority)) return true;
//	        return introduction != null && !introduction.trim().isEmpty();
//	    }

	    @AssertTrue(message = "{error.introduction}")
	    public boolean isIntroductionLengthValid() {
	        if ("1".equals(authority)) return true;
	        return introduction == null || introduction.length() <= 1500;
	    }

	 // アップロードされたファイルを受け取る用（既にあるやつ）
	    private MultipartFile profileImage;

	    // 表示・保存用のファイル名（Stringとして保存されてる画像名）
	    private String profileImageName;

//	    @AssertTrue(message = "{error.profileImage.required}")
//	    public boolean isProfileImageSelected() {
//	        if ("1".equals(authority)) return true;
//	        return profileImage != null && !profileImage.isEmpty();
//	    }

	    @AssertTrue(message = "{error.profileImage.size}")
	    public boolean isProfileImageSizeValid() {
	        return profileImage == null || profileImage.getSize() <= (2 * 1024 * 1024);
	    }
	    
	    private String originalLoginId;

	    public String getOriginalLoginId() {
	        return originalLoginId;
	    }

	    public void setOriginalLoginId(String originalLoginId) {
	        this.originalLoginId = originalLoginId;
	    }

}

