package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.form.AskForm;
import com.example.demo.model.Category; // ✅ ← 追加
import com.example.demo.model.Inquiry;
import com.example.demo.model.InquiryStatus;
import com.example.demo.repository.CategoryRepository; // ✅ ← 追加
import com.example.demo.repository.InquiryRepository;
import com.example.demo.repository.UserInfoRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.LikeService;
import com.example.demo.service.MailService;
import com.example.demo.service.UserService;

@Controller
public class PublicAccountController {

    private final UserService userService;
    private final LikeService likeService;
    private final CategoryService categoryService;
    private final MailService mailService;
    private final UserInfoRepository userInfoRepository;
    private final InquiryRepository inquiryRepository;
    private final CategoryRepository categoryRepository; // ✅ ← フィールド追加

    public PublicAccountController(UserService userService,
                                   LikeService likeService,
                                   CategoryService categoryService,
                                   MailService mailService,
                                   UserInfoRepository userInfoRepository,
                                   InquiryRepository inquiryRepository,
                                   CategoryRepository categoryRepository) { // ✅ ← 引数追加
        this.userService = userService;
        this.likeService = likeService;
        this.categoryService = categoryService;
        this.mailService = mailService;
        this.userInfoRepository = userInfoRepository;
        this.inquiryRepository = inquiryRepository;
        this.categoryRepository = categoryRepository; // ✅ ← 代入
    }

    // 全期間ランキング（一般ユーザーのみ）
    @GetMapping("/publicUsers")
    public String showPublicUserList(Model model) {
        var users = userService.getAllUsers().stream()
                .filter(u -> "2".equals(u.getAuthority()))
                .sorted((u1, u2) -> Integer.compare(u2.getLikes(), u1.getLikes()))
                .toList();

        model.addAttribute("generalUsers", users);
        model.addAttribute("categoryList", categoryService.getAllCategories());
        model.addAttribute("activeTab", "all");
        model.addAttribute("askForm", new AskForm());

        return "publicPage/userPublicList";
    }

    // 月間ランキング（一般ユーザーのみ）
    @GetMapping("/publicUsers/monthly")
    public String showMonthlyRankedUsers(
            @RequestParam(value = "month", required = false) String monthStr,
            Model model) {

        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        if (monthStr != null && !monthStr.isEmpty()) {
            try {
                String[] parts = monthStr.split("-");
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
            } catch (Exception e) {
                // 無視して今月のまま
            }
        }

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);

        var ranking = likeService.getLikeRankingBetween(start, end);

        var users = ranking.stream()
                .map(r -> {
                    var user = userService.getUserById((String) r[0]);
                    if (user != null && "2".equals(user.getAuthority())) {
                        user.setLikes(((Number) r[1]).intValue());
                        return user;
                    }
                    return null;
                })
                .filter(u -> u != null)
                .toList();

        model.addAttribute("monthlyUsers", users);
        model.addAttribute("categoryList", categoryService.getAllCategories());
        model.addAttribute("activeTab", "monthly");
        model.addAttribute("askForm", new AskForm());
        model.addAttribute("selectedMonth", String.format("%04d-%02d", year, month));
        model.addAttribute("availableMonths", likeService.getAvailableMonths());

        return "publicPage/userMonthlyList";
    }

    // いいね
    @PostMapping("/public/like")
    @ResponseBody
    public String likeUser(@RequestParam String loginId) {
        int newLikes = userService.incrementLikes(loginId);
        return String.valueOf(newLikes);
    }

    // お問い合わせ（公開ページ用）
    @PostMapping("/publicUsers/ask")
    public String postAsk(@ModelAttribute AskForm askForm, Model model) {
        model.addAttribute("categoryList", categoryService.getAllCategories());
        model.addAttribute("activeTab", "all");

        var users = userService.getAllUsers().stream()
                .filter(u -> "2".equals(u.getAuthority()))
                .sorted((u1, u2) -> Integer.compare(u2.getLikes(), u1.getLikes()))
                .toList();
        model.addAttribute("generalUsers", users);

        // 📧 件名
        String subject = "【自動応答メール】お問い合わせ受付のお知らせ";

        // 📧 本文テンプレート
        String body = String.format(
            """
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
            askForm.getEmail(),
            askForm.getEmail(),
            askForm.getCategory(),
            askForm.getSubject(),
            askForm.getMessage()
        );

        // 📬 メール送信
        mailService.sendMail(askForm.getEmail(), subject, body);
        model.addAttribute("message", "お問い合わせ内容をメールで送信しました！");

        // 🗃️ DB保存（カテゴリ変換を含む）
        Inquiry inquiry = new Inquiry();

        Optional<Category> categoryOpt = categoryRepository.findByName(askForm.getCategory()); // ✅
        if (categoryOpt.isEmpty()) {
            model.addAttribute("error", "カテゴリが見つかりませんでした。");
            return "publicPage/userPublicList";
        }

        inquiry.setCategory(categoryOpt.get()); // ✅
        inquiry.setSubject(askForm.getSubject());
        inquiry.setMessage(askForm.getMessage());
        inquiry.setEmail(askForm.getEmail());
        inquiry.setStatus(InquiryStatus.未対応);
        inquiry.setCreatedAt(LocalDateTime.now());

        inquiryRepository.save(inquiry);

        return "redirect:/publicUsers?ask=success";
    }
}
