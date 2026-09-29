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
import com.example.demo.entity.UserInfo;
import com.example.demo.form.SignupForm;
import com.example.demo.service.SignupService;
import com.example.demo.util.AppUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AddController {

    /** ユーザー登録処理サービス */
    private final SignupService service;

    /** メッセージソース（messages.properties連携） */
    private final MessageSource messageSource;

    /**
     * ユーザー新規登録画面の初期表示
     * @param model モデル
     * @param form 入力フォーム
     * @return ユーザー登録画面
     */
    @GetMapping("/add")
    public String view(Model model, SignupForm form) {
        if (form.getAuthority() == null) {
            form.setAuthority("1"); // 管理者をデフォルトに
        }
        model.addAttribute("signupForm", form); // 入力内容を反映（リダイレクト後も維持）
        return "admin/add";
    }

    /**
     * ユーザー登録処理
     * @param model モデル
     * @param form 入力情報
     * @param result バリデーション結果
     * @return 登録画面
     */
    @PostMapping("/add")
    public String registerUser(Model model, @Validated SignupForm form, BindingResult result) {

        // ✅ プロフィール画像のサイズチェック（2MB以内）
        MultipartFile profileImage = form.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            if (profileImage.getSize() > 2 * 1024 * 1024) {
                result.rejectValue("profileImage", "error.profileImage.size");
                edtGuideMessage(model, "common.formError", true);
                model.addAttribute("signupForm", form);
                return "admin/add";
            }
        }
    	
        // バリデーションエラーがある場合はそのまま再表示
        if (result.hasErrors()) {
            edtGuideMessage(model, MessageConst.FORM_ERROR, true);
            model.addAttribute("signupForm", form);
            return "admin/add";
        }


        // プロフィール画像の保存処理
        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                String fileName = service.saveProfileImage(profileImage, form.getLoginId());
                form.setProfileImageName(fileName);
            } else {
                form.setProfileImageName("default.jpg"); // 画像未選択ならデフォルト
            }
        } catch (IOException e) {
            model.addAttribute("message", "プロフィール画像の保存に失敗しました。");
            model.addAttribute("isError", true);
            model.addAttribute("signupForm", form);
            return "admin/add";
        }

        // ユーザー情報の登録処理・成功/失敗の表示
        Optional<UserInfo> userInfoOpt = service.resistUserInfo(form);
        SignupMessage signupMessage = judgeMessageKey(userInfoOpt);
        edtGuideMessage(model, signupMessage.getMessageId(), signupMessage.isError());

        if (signupMessage.isError()) {
            model.addAttribute("signupForm", form);
            return "admin/add";
        }

        // 成功時：フォームをリセットして再表示
        model.addAttribute("signupForm", new SignupForm());
        return "admin/add";
    }


    /**
     * メッセージをモデルに設定
     */
    private void edtGuideMessage(Model model, String messageId, boolean isError) {
        String message = AppUtil.getMessage(messageSource, messageId);
        model.addAttribute("message", message);
        model.addAttribute("isError", isError);
    }

    /**
     * 登録結果に応じてメッセージキーを返す
     */
    private SignupMessage judgeMessageKey(Optional<UserInfo> userInfoOpt) {
        return userInfoOpt.isEmpty() ? SignupMessage.EXISTED_LOGIN_ID : SignupMessage.SUCCEED;
    }
}
