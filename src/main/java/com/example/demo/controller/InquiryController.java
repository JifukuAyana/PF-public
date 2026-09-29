package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StatusUpdateRequest;
import com.example.demo.model.Inquiry;
import com.example.demo.model.InquiryStatus;
import com.example.demo.service.InquiryService;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {
    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    // 問い合わせ作成
    @PostMapping
    public Inquiry createInquiry(@RequestBody Inquiry inquiry) {
        return inquiryService.saveInquiry(inquiry);
    }

    // すべて取得
    @GetMapping
    public List<Inquiry> getAllInquiries() {
        return inquiryService.getAllInquiries();
    }

    // カテゴリで絞り込み
    @GetMapping("/category")
    public ResponseEntity<List<Inquiry>> getInquiriesByCategory(@RequestParam Long id) {
        List<Inquiry> inquiries = inquiryService.getInquiriesByCategoryId(id);
        return ResponseEntity.ok(inquiries);
    }

    // ステータス更新
    @PutMapping("/{id}")
    public ResponseEntity<Inquiry> updateInquiryStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {

        Optional<Inquiry> inquiryOpt = inquiryService.getInquiryById(id);

        if (inquiryOpt.isPresent()) {
            Inquiry inquiry = inquiryOpt.get();
            try {
                inquiry.setStatus(InquiryStatus.valueOf(request.getStatus())); // ← 修正ここ！
                inquiryService.saveInquiry(inquiry);
                return ResponseEntity.ok(inquiry);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(null); // 変換できなかった場合の対処（nullでもOK）
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    // IDで取得
    @GetMapping("/{id}")
    public ResponseEntity<?> getInquiryById(@PathVariable Long id) {
        try {
            Optional<Inquiry> inquiryOpt = inquiryService.getInquiryById(id);

            if (inquiryOpt.isPresent()) {
                return ResponseEntity.ok(inquiryOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("Error: 問い合わせID " + id + " は存在しません");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("サーバー内部エラー: " + e.getMessage());
        }
    }
}
