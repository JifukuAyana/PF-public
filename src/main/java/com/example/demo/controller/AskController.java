package com.example.demo.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.UserInfo;
import com.example.demo.form.AskForm;
import com.example.demo.model.Category; // ✅ 追加
import com.example.demo.model.Inquiry;
import com.example.demo.model.InquiryStatus;
import com.example.demo.repository.CategoryRepository; // ✅ 追加
import com.example.demo.repository.InquiryRepository;
import com.example.demo.repository.UserInfoRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.MailService;

@Controller
@RequestMapping("/ask")
public class AskController {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MailService mailService;

    @Autowired
    private CategoryRepository categoryRepository; // ✅ 追加

    @GetMapping
    public String showAskForm(Model model) {
        model.addAttribute("askForm", new AskForm());
        model.addAttribute("categoryList", categoryService.getAllCategories());
        return "ask";
    }

    @PostMapping
    public String postAsk(@ModelAttribute AskForm askForm, Model model, Principal principal) {
        String loginId = principal.getName();
        model.addAttribute("askForm", askForm);
        model.addAttribute("categoryList", categoryService.getAllCategories());

        if (askForm.getCategory() == null || askForm.getCategory().isEmpty()) {
            model.addAttribute("error", "カテゴリを選択してください。");
            return "ask";
        }

        // ✅ カテゴリ名から Category を取得
        Optional<Category> categoryOpt = categoryRepository.findByName(askForm.getCategory());
        if (categoryOpt.isEmpty()) {
            model.addAttribute("error", "指定されたカテゴリが見つかりませんでした。");
            return "ask";
        }

        // メール送信処理
        Optional<UserInfo> userOpt = userInfoRepository.findByLoginId(loginId);
        if (userOpt.isPresent()) {
            String mailTo = userOpt.get().getMail();
            if (mailTo != null && !mailTo.isEmpty()) {
                String subject = "【自動応答メール】お問い合わせ受付のお知らせ";
                String body = String.format("""
                        ※このメールはシステムからの自動返信です。

                        %s 様

                        お問い合わせをいただきありがとうございます。
                        以下の内容でお問い合わせを受け付けました。
                        3営業日以内に、担当者よりご連絡いたしますので今しばらくお待ちください。

                        ━━━□■□　お問い合わせ内容　□■□━━━
                        E-Mail：%s
                        カテゴリ：%s
                        件名：%s
                        お問い合わせ内容：
                        %s
                        ━━━━━━━━━━━━━━━━━━━━━
                        """,
                        loginId,
                        mailTo,
                        askForm.getCategory(),
                        askForm.getSubject(),
                        askForm.getMessage()
                );

                mailService.sendMail(mailTo, subject, body);
                model.addAttribute("message", "お問い合わせ内容をメールで送信しました！");
            } else {
                model.addAttribute("error", "メールアドレスが取得できませんでした。");
                return "ask";
            }
        } else {
            model.addAttribute("error", "ログインユーザーの情報が見つかりませんでした。");
            return "ask";
        }

        // ✅ カテゴリをエンティティとしてセットして保存
        Inquiry inquiry = new Inquiry();
        inquiry.setCategory(categoryOpt.get());
        inquiry.setSubject(askForm.getSubject());
        inquiry.setMessage(askForm.getMessage());
        inquiry.setEmail(userOpt.map(UserInfo::getMail).orElse(null));
        inquiry.setStatus(InquiryStatus.未対応);
        inquiry.setCreatedAt(LocalDateTime.now());

        inquiryRepository.save(inquiry);

        return "ask";
    }
}
