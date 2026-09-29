package com.example.demo.controller.admin;

import java.io.IOException;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.constant.MessageConst;
import com.example.demo.constant.SignupMessage;
import com.example.demo.constant.UrlConst;
import com.example.demo.entity.UserInfo;
import com.example.demo.form.SignupForm;
import com.example.demo.service.SignupService;
import com.example.demo.util.AppUtil;

import lombok.RequiredArgsConstructor;


@Controller // 必須：コントローラーであることを明示
@RequiredArgsConstructor
public class SignupController {
	
	/** ユーザー登録画面 Service */
	private final SignupService service;

	/** メッセージソース */
	private final MessageSource messageSource;


	/**
	 * 初期表示
	 *
	 * @param model モデル
	 * @param form 入力情報
	 * @return 表示画面
	 */
	@GetMapping(UrlConst.SIGNUP)
	public String view(Model model, SignupForm form) {
	    if (form.getAuthority() == null) {
	        form.setAuthority("1"); // ← 管理者を初期値にする
	    }
//	    signup画面だけ「ログイン画面へ」の表示にする。
	    model.addAttribute("signupForm", form);
	    model.addAttribute("isSignupPage", true); 
	    return "signup";
	}



	/**
	 * ユーザー登録
	 * 
	 * @param model モデル
	 * @param form 入力情報
	 * @param bdResult 入力チェック結果
	 * @return 表示画面
	 */
	@PostMapping(UrlConst.SIGNUP)
	public String signup(Model model, @Validated SignupForm form, BindingResult bdResult) {
		
		  // ✅ 最初に画像サイズチェック（先にしないとスキップされる）
	    MultipartFile profileImage = form.getProfileImage();
	    if (profileImage != null && !profileImage.isEmpty()) {
	        if (profileImage.getSize() > 2 * 1024 * 1024) {
	            bdResult.rejectValue("profileImage", "error.profileImage.size");
	        }
	    }

	    // ✅ 次にエラーチェック（すべてのエラーをまとめて表示）
	    if (bdResult.hasErrors()) {
	        edtGuideMessage(model, MessageConst.FORM_ERROR, true);
	        model.addAttribute("signupForm", form);
	        return "signup";
	    }


	    // ✅ プロフィール画像の保存
	    try {
	        if (profileImage != null && !profileImage.isEmpty()) {
	            String fileName = service.saveProfileImage(profileImage, form.getLoginId());
	            form.setProfileImageName(fileName);
	        } else {
	            form.setProfileImageName("default.jpg");
	        }
	    } catch (IOException e) {
	        model.addAttribute("message", "プロフィール画像の保存に失敗しました。");
	        model.addAttribute("isError", true);
	        return "signup";
	    }
	    
	    
        // ユーザー情報の登録
	    var userInfoOpt = service.resistUserInfo(form);
	    var signupMessage = judgeMessageKey(userInfoOpt);
//	    var messageId = AppUtil.getMessage(messageSource, signupMessage.getMessageId());
//	    model.addAttribute("message", messageId);
//	    model.addAttribute("isError", signupMessage.isError());
	    
	    
	    edtGuideMessage(model, signupMessage.getMessageId(), signupMessage.isError());
	    
	 // 登録成功ならログイン画面やTOPに遷移（今回はsignup画面のまま残る）
	    if (signupMessage.isError()) {
	        model.addAttribute("signupForm", form);
	        return "signup";
	    } else {
	        model.addAttribute("signupForm", new SignupForm()); // フォームをリセットして再表示
	        return "signup"; // signup.htmlのまま
	    }
	}
	
	
	/**
	 * 画面に表示するガイドメッセージを設定する
	 *
	 * @param model モデル
	 * @param messageId メッセージID
	 * @param isError エラーフラグ
	 */
	private void edtGuideMessage(Model model, String messageId, boolean isError) {
	    var message = AppUtil.getMessage(messageSource, messageId);
	    model.addAttribute("message", message);
	    model.addAttribute("isError", isError);
	}

	

	/**
	 * ユーザ情報登録の結果メッセージキーを判定する
	 *
	 * @param userInfoOpt ユーザ登録結果（登録済みの場合はEmpty）
	 * @return メッセージキー
	 */
	private SignupMessage judgeMessageKey(Optional<UserInfo> userInfoOpt) {
	    if (userInfoOpt.isEmpty()) {
	        return SignupMessage.EXISTED_LOGIN_ID;
	    } else {
	        return SignupMessage.SUCCEED;
	    }
	}
	
}